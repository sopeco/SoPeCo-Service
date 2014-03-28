/**
 * Copyright (c) 2013 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.service.execute;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.config.IConfiguration;
import org.sopeco.engine.status.ErrorInfo;
import org.sopeco.engine.status.EventType;
import org.sopeco.engine.status.IStatusListener;
import org.sopeco.engine.status.ProgressInfo;
import org.sopeco.engine.status.StatusBroker;
import org.sopeco.engine.status.StatusMessage;
import org.sopeco.runner.SoPeCoRunner;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.exchange.ExperimentStatus;

/**
 * @author Marius Oehler
 * @author Peter Merkert
 */
public class ExecutionQueue implements IStatusListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionQueue.class);
	
	/**
	 * Handles all the thread of SoPeCo Runner executions.
	 */
	private ExecutorService threadPool;

	/** 
	 * Queue of waiting experiments.
	 */
	private List<QueuedExperiment> experimentQueue;

	/**
	 * The experiment which is performed at the moment.
	 */
	private QueuedExperiment runningExperiment;

	/**
	 * If this object is not <code>null</code>, then an experiment is running.
	 */
	private Future<?> executeStatus;

	/**
	 * The URL for this execution queue. All experiment run on the controller connected to this URL.
	 */
	private String controllerURL;

	/**
	 * Stores the current hashcode of the running experiment (actually the {@link ScheduledExperiment} hashcode).
	 * This hashcode enables to have a unique identifier for the {@link Configuration}.
	 * 
	 *  This hashcode is always refreshed with the current hashcode of the running {@link QueuedExperiment} and
	 *  it's {@link ScheduledExperiment} hashcode.
	 */
	private String experimentHashCode;

	/**
	 * Constructor creates a new empty {@link QueuedExperiment} list.
	 * Stores the given controller URL. <br />
	 * Adds itself to the {@link StatusManager} to receive updates about
	 * the running experiments.<br />
	 * Initializes the threadpool via initThreadPool().
	 * 
	 * @param controllerURL the URL to the controller this queue correpsonds to
	 */
	public ExecutionQueue(String controllerURL) {
		this.experimentQueue 	= new ArrayList<QueuedExperiment>();
		this.controllerURL 		= controllerURL;

		// register to the StatusManager and recevie updates from SoPeCo about this controller
		StatusBroker.getManager(controllerURL).addStatusListener(this);
		initThreadPool();
	}
	
	/**
	 * Returns whether the active experiment is executed by a thread.
	 * 
	 * @return true, if this queue has currently an experiment running
	 */
	public boolean isExecuting() {
		
		if (executeStatus != null) {
			
			synchronized (executeStatus) {
				
				if (executeStatus.isDone() || executeStatus.isCancelled()) {
					return false;
				}
				
				return true;
			}
			
		}
		
		return false;
	}

	/**
	 * Returns the {@link ExperimentStatus} of the {@link QueuedExperiment} with the
	 * given experiment key.
	 * 
	 * @param experimentKey the unique key to the experiment, which is returned when adding
	 * 						it to the {@link ExecutionScheduler}.
	 * @return				the {@link ExperimentStatus} of the {@link QueuedExperiment}
	 */
	public ExperimentStatus getExperimentStatus(long experimentKey) {
		
		QueuedExperiment experiment = null;
		
		if (experimentHashCode == String.valueOf(experimentKey)) {
			
			// the current running experiment is requested
			experiment = runningExperiment;
			
		} else {
			
			// check if another experiment in the queue was requested
			for (QueuedExperiment exp : experimentQueue) {
				
				if (String.valueOf(exp.getScheduledExperiment().getExperimentKey()).equals(experimentKey)) {
					experiment = exp; // found the searched experiment
					break;
				}
				
			}
			
		}
		
		if (experiment != null) {
			return createExperimentStatusPackage(experiment);
		}
		
		return null;
		
	}
	
	
	/**
	 * Adds an experiment to this {@link ExecutionQueue}. If no experiment is executed
	 * yet, a new one will be executed.
	 * 
	 * @param experiment the {@link QueuedExperiment} to add to the queue.
	 */
	public void addExperiment(QueuedExperiment experiment) {
		LOGGER.info("Adding experiment id:" + experiment.getScheduledExperiment().getId() + " to queue.");
		experiment.setTimeQueued(System.currentTimeMillis());
		experimentQueue.add(experiment);
		
		
		
		checkToExecuteNext(); //TODO not nice style to execute here!
	}

	/**
	 * Abots the current experiment in execution.
	 */
	public void abortExperiment(long experimentKey) {
		
		if (isExecuting()) {
			Configuration.getSessionSingleton(String.valueOf(experimentKey))
						 .setProperty(IConfiguration.EXPERIMENT_RUN_ABORT, new Boolean(true));
		}

	}

	/**
	 * Checks if the controller is ready and a experiment is waiting in the
	 * queue. If so, the next experiment is started.
	 */
	private void checkToExecuteNext() {
		
		synchronized (experimentQueue) {
			
			LOGGER.debug("Checking the current execution of the ExecutionQueue corresponding to Controller URL '{}'", controllerURL);
			
			if (isExecuting()) {
				
				LOGGER.info("Experiment is currently already running.");
				
			} else if (isLoaded()) {
				
				LOGGER.info("Experiment is already loaded, but it is not running yet.");
				
			} else if (experimentQueue.isEmpty()) {
				
				LOGGER.info("Queue is empty. There is no experiment in execution.");
				
			} else {
				
				// now the queue is not empty and it the next experimetn can be executed
				runningExperiment = experimentQueue.get(0);
				experimentQueue.remove(0);
				
				execute(runningExperiment);
				
			}
		}
	}

	/**
	 * TODO: this method should not be possible. Be aware otherwise of side-effects!
	 * 
	 * Returns the experiment which is loaded at the moment.
	 * 
	 * @return QueuedExperiment
	 */
	/*public QueuedExperiment getCurrentlyRunning() {
		return runningExperiment;
	}*/

	/**
	 * Adds the given {@link StatusMessage} to the currently running experiment.
	 * If this queue has no active experiment, then the message is discarded.
	 * 
	 * @param statusMessage the message which should be added to the experiment
	 */
	public void addStatusMessageToExperiment(StatusMessage statusMessage) {
		if (runningExperiment == null) {
			return;
		}
		
		runningExperiment.getStatusMessageList().add(statusMessage);
		if (statusMessage.getStatusInfo() != null && statusMessage.getStatusInfo() instanceof ProgressInfo) {
			runningExperiment.setLastProgressInfo((ProgressInfo) statusMessage.getStatusInfo());
		}
		
		storeMECLog(runningExperiment);

	}

	/**
	 * Checks for failed adding of {@link EventType.MEASUREMENT_FINISHED}. Fires this
	 * event manually with a {@link StatusMessage}.
	 * 
	 * Marked as TODO for Marius Öhler. Why is this method needed?
	 */
	public void check() {
		if (!isExecuting() && runningExperiment != null) {
			LOGGER.debug("Thread finished but experiment was not completed. Adding MEASUREMENT_FINISHED event.");
			StatusMessage sm = new StatusMessage();
			sm.setEventType(EventType.MEASUREMENT_FINISHED);
			onNewStatus(sm);
		}
	}

	/**
	 * The method for the {@link IStatusListener} which is called by the {@link StatusManager}
	 * to fire status messages for the executed controllers.
	 * 
	 * @param statusMessage the status message to proceed
	 */
	@Override
	public void onNewStatus(StatusMessage statusMessage) {
		LOGGER.info("New Status on '" + this.controllerURL + "': " + statusMessage.getEventType());
		
		addStatusMessageToExperiment(statusMessage);

		if (statusMessage.getEventType() == EventType.EXECUTION_FAILED) {
			LOGGER.warn("Experiment could not be executed succesfully. Status: Execution failed");
		}

		if (statusMessage.getEventType() == EventType.MEASUREMENT_FINISHED) {
			processFinishedExperiment();
		}
	}

	/**
	 * Ends the execution of the current experiment and stores information about
	 * it in the database.<br />
	 * Afterwards it's tried to execute the next experiment.
	 */
	private synchronized void processFinishedExperiment() {
		LOGGER.info("Experiment id:" + runningExperiment.getScheduledExperiment().getId()
				    + " finished on: " + runningExperiment.getScheduledExperiment().getControllerUrl());

		runningExperiment.setTimeEnded(System.currentTimeMillis());
		
		storeDurationInExperiment();

		storeExecutedExperimentDetails(runningExperiment);
		storeMECLog(runningExperiment);
		
		Configuration.removeConfiguration(experimentHashCode);

		executeStatus 		= null;
		runningExperiment 	= null;
		experimentHashCode 	= "";
		
		// now the next experiment can be executed
		checkToExecuteNext();
	}




	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// HELPER ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates the {@link ExperimentStatus} object, that contains all
	 * necessary information about the current controller state.
	 * 
	 * @param experiment 	the experiment, whose details are requested
	 * @return 				a status package with all informatio about the currently running experiment
	 */
	private ExperimentStatus createExperimentStatusPackage(QueuedExperiment experiment) {
		
		if (experiment == null) {
			return null;
		}
		
		ExperimentStatus cce = new ExperimentStatus();

		cce.setAccountId(runningExperiment.getScheduledExperiment().getAccountId());
		cce.setScenarioName(runningExperiment.getScheduledExperiment().getScenarioDefinition().getScenarioName());
		cce.setTimeStart(runningExperiment.getTimeStarted());
		cce.setLabel(runningExperiment.getScheduledExperiment().getLabel());
		cce.setEventLogList(runningExperiment.getEventLogLiteList());

		if (runningExperiment.getStatusMessageList().get(runningExperiment.getStatusMessageList().size() - 1)
				.getEventType() == EventType.MEASUREMENT_FINISHED) {
			cce.setFinished(true);
		} else {
			cce.setFinished(false);
		}

		if (runningExperiment.getLastProgressInfo() != null) {
			ProgressInfo info = runningExperiment.getLastProgressInfo();
			final float maxPercentage = 100F;
			float progress = maxPercentage / info.getNumberOfRepetition() * info.getRepetition();
			cce.setProgress(progress);
		} else {
			// then the experiment has not started yet
			cce.setProgress(0.0f);
		}

		if (runningExperiment.getScheduledExperiment().getDurations().size() > 2) {
			long sum = 0;
			for (long l : runningExperiment.getScheduledExperiment().getDurations()) {
				sum += l;
			}
			long estiamtedDuration = sum / runningExperiment.getScheduledExperiment().getDurations().size();
			cce.setTimeRemaining(estiamtedDuration);
		}

		return cce;
	}
	
	/**
	 * Creates an threadpool, which is responsible for the SoPeCo Runners. This is a singleton method
	 * for the threadpool and currently a cached thread pool is created, which has a varying numbers
	 * of thread in it.
	 * 
	 * @return the new created {@link ExecutorService}
	 */
	private ExecutorService initThreadPool() {
		if (threadPool == null) {
			threadPool = Executors.newCachedThreadPool();
		}
		return threadPool;
	}
	
	/**
	 * Returns whether an experiment is loaded. This means, thaht an experiment has been
	 * submitted to the threadpool to be executed in a {@link SoPeCoRunner}.<br />
	 * <br />
	 * This method does <b>not</b> query, if an experiment is currently really executed!
	 * 
	 * @return true, if an experiment is loaded
	 */
	private boolean isLoaded() {
		if (runningExperiment != null) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Start the next execution. Only called by <code>checkToExecuteNext()</code>!
	 * 
	 * @param experiment the experiment to execute next
	 */
	private void execute(QueuedExperiment experiment) {
		LOGGER.info("Start experiment id:" + experiment.getScheduledExperiment().getId()
					+ " on: " + experiment.getScheduledExperiment().getControllerUrl());

		// prepare execution properties
		Map<String, Object> executionProperties = new HashMap<String, Object>();
		
		try {
			// copy all the current experiment properties
			executionProperties.putAll(experiment.getScheduledExperiment().getProperties());
			
			LOGGER.debug("Experiement settings controller URL: '{}'", experiment.getScheduledExperiment().getControllerUrl());
			
			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_URI, new URI(experiment.getScheduledExperiment().getControllerUrl()));
			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_CLASS_NAME, null); // only if class name is null, URI is searched
			executionProperties.put(IConfiguration.CONF_SCENARIO_DESCRIPTION, experiment.getScheduledExperiment().getScenarioDefinition());
			
		} catch (URISyntaxException e) {
			LOGGER.error("Invalid controller URL '{}'.", experiment.getScheduledExperiment().getControllerUrl());
		}
		
		experimentHashCode = String.valueOf(experiment.getScheduledExperiment().getExperimentKey());
		
		//TODO check the selected experiments!!
		SoPeCoRunner runner = new SoPeCoRunner(experimentHashCode,
											   executionProperties,
											   experiment.getScheduledExperiment().getSelectedExperiments());
		
		/* Example execution for test cases.
		// fetch example experiement to execute
		exp.setSelectedExperiments(new ArrayList<String>(Arrays.asList("experimentSeriesDefintion")));	
		SoPeCoRunner runner  = new SoPeCoRunner(usertoken,
												executionProperties,
												exp.getSelectedExperiments());
		*/
		
		executeStatus = threadPool.submit(runner);

		experiment.setTimeStarted(System.currentTimeMillis());
		
	}
	
	/**
	 * Stores the duration of this execution in the list, which is stored in the
	 * ScheduledExperiment.
	 */
	private void storeDurationInExperiment() {
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance()
															.loadScheduledExperiment(runningExperiment.getScheduledExperiment().getId());
		
		if (exp != null) {
			
			if (exp.getDurations() == null) {
				exp.setDurations(new ArrayList<Long>());
			}
			
			long duration = runningExperiment.getTimeEnded() - runningExperiment.getTimeStarted();
			exp.getDurations().add(duration);
			
			ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		}
		
	}
	
	/**
	 * Stores the final results for the exectured experiment in the database as
	 * an {@link ExecutedExperimentDetails} object. 
	 */
	private void storeExecutedExperimentDetails(QueuedExperiment experiment) {
		
		boolean hasError = false;
		
		// check for errors in the experiment
		for (StatusMessage sm : experiment.getStatusMessageList()) {
			if (sm.getStatusInfo() != null && sm.getStatusInfo() instanceof ErrorInfo) {
				hasError = true;
				break;
			}
		}

		// TODO maybe merge ID and ExperimentKey, as both are unique
		ExecutedExperimentDetails eed = new ExecutedExperimentDetails();
		eed.setSuccessful(!hasError);
		eed.setTimeFinished(experiment.getTimeEnded());
		eed.setTimeStarted(experiment.getTimeStarted());
		eed.setName(experiment.getScheduledExperiment().getLabel());
		eed.setControllerURL(experiment.getScheduledExperiment().getControllerUrl());
		eed.setExperimentKey(experiment.getScheduledExperiment().getExperimentKey());
		eed.setAccountId(experiment.getScheduledExperiment().getAccountId());
		eed.setScenarioName(experiment.getScheduledExperiment().getScenarioDefinition().getScenarioName());
		// TODO why was commented out? Why is the event log not set - Answer: Because the MECLog is stored on it's own
		// eed.setEventLog(runningExperiment.getEventLogLiteList());

		ServicePersistenceProvider.getInstance().storeExecutedExperimentDetails(eed);
	}
	
	/**
	 * Stores the {@link MECLog} for the the given {@link QueuedExperiment}. When A MECLog with the
	 * experiment hashcode has already existed, then it's updated.
	 * 
	 * @param experiment the experiment to store the {@link MECLog} to
	 */
	private void storeMECLog(QueuedExperiment experiment) {
		
		long hashcode = experiment.getScheduledExperiment().getExperimentKey();
		
		MECLog log = ServicePersistenceProvider.getInstance().loadMECLog(hashcode);
		
		if (log == null) {
			log = new MECLog();
			log.setId(hashcode);
		}
		
		log.setEntries(experiment.getEventLogLiteList());

		ServicePersistenceProvider.getInstance().storeMECLog(log);
	}
	
}
