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
import org.sopeco.engine.status.StatusManager;
import org.sopeco.engine.status.StatusMessage;
import org.sopeco.runner.SoPeCoRunner;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.shared.RunningControllerStatus;

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

	private Future<?> executeStatus;

	private String controllerURL;

	private String experimentHashCode;

	/**
	 * Constructor creates a new empty {@link QueuedExperiment} list.
	 * Stores the given controller URL. <br />
	 * Adds itself to the {@link StatusManager} to receive updates about
	 * the running experiments.
	 */
	public ExecutionQueue(String pControllerURL) {
		experimentQueue = new ArrayList<QueuedExperiment>();
		controllerURL = pControllerURL;

		StatusBroker.getManager(pControllerURL).addStatusListener(this);
	}
	
	/**
	 * Creates an ThreadPool, which is responsible for the SoPeCo Runners.
	 * 
	 * @return ExecutorService
	 */
	private ExecutorService getThreadPool() {
		if (threadPool == null) {
			threadPool = Executors.newCachedThreadPool();
		}
		return threadPool;
	}

	/**
	 * Returns whether the active experiment is executed by a thread.
	 * 
	 * @return true, if the SoPeCo currently runs an experiment
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
	 * Returns whether an experiment is loaded, and perhaps is executed.
	 * 
	 * @return true, if an experiment is active
	 */
	public boolean isLoaded() {
		if (runningExperiment != null) {
			return true;
		}
		
		return false;
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
		checkToExecuteNext();
	}

	/**
	 * Abots the current experiment in execution.
	 */
	public void abortExperiment() {
		
		if (isExecuting()) {
			Configuration.getSessionSingleton(experimentHashCode)
						 .setProperty(IConfiguration.EXPERIMENT_RUN_ABORT, new Boolean(true));
		}

	}

	/**
	 * Checks if the controller is ready and a experiment is waiting in the
	 * queue. If so, the next experiment is started.
	 */
	private void checkToExecuteNext() {
		
		synchronized (experimentQueue) {
			
			LOGGER.debug("Looking for waiting experiment..");
			
			if (isExecuting()) {
				
				LOGGER.info("Controller is running.");
				
			} else if (isLoaded()) {
				
				LOGGER.info("Experiment is already loaded.");
				
			} else if (experimentQueue.isEmpty()) {
				
				LOGGER.info("Queue is empty.");
				
			} else {
				
				runningExperiment = experimentQueue.get(0);
				experimentQueue.remove(0);
				
				executeNext();
				
			}
		}
	}

	/**
	 * Start the next execution. 
	 */
	private void executeNext() {
		LOGGER.info("Start experiment id:" + runningExperiment.getScheduledExperiment().getId()
					+ " on: " + runningExperiment.getScheduledExperiment().getControllerUrl());

		// prepare execution properties
		Map<String, Object> executionProperties = new HashMap<String, Object>();
		
		try {
			// copy all the current experiment properties
			executionProperties.putAll(runningExperiment.getScheduledExperiment().getProperties());
			
			LOGGER.debug("Experiement settings controller URL: '{}'", runningExperiment.getScheduledExperiment().getControllerUrl());
			
			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_URI, new URI(runningExperiment.getScheduledExperiment().getControllerUrl()));
			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_CLASS_NAME, null); // only if class name is null, URI is searched
			executionProperties.put(IConfiguration.CONF_SCENARIO_DESCRIPTION, runningExperiment.getScheduledExperiment().getScenarioDefinition());
			
		} catch (URISyntaxException e) {
			LOGGER.error("Invalid controller URL '{}'.", runningExperiment.getScheduledExperiment().getControllerUrl());
		}
		
		experimentHashCode = String.valueOf(runningExperiment.getScheduledExperiment().hashCode());
		
		//TODO check the selected experiments!!
		SoPeCoRunner runner = new SoPeCoRunner(experimentHashCode,
											   executionProperties,
											   runningExperiment.getScheduledExperiment().getSelectedExperiments());
		
		/* Example execution for test cases.
		// fetch example experiement to execute
		exp.setSelectedExperiments(new ArrayList<String>(Arrays.asList("experimentSeriesDefintion")));	
		SoPeCoRunner runner  = new SoPeCoRunner(usertoken,
												executionProperties,
												exp.getSelectedExperiments());
		*/
		
		executeStatus = getThreadPool().submit(runner);

		runningExperiment.setTimeStarted(System.currentTimeMillis());
		
	}

	/**
	 * Stores the duration of this execution in the list, which is stored in the
	 * ScheduledExperiment.
	 */
	private void saveDurationInExperiment() {
		
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
	 * Returns the experiment which is loaded at the moment.
	 * 
	 * @return QueuedExperiment
	 */
	public QueuedExperiment getCurrentlyRunning() {
		return runningExperiment;
	}

	/**
	 * Pushes the next status of the running experiment.
	 * 
	 * @param statusMessage
	 */
	public void nextStatusMessage(StatusMessage statusMessage) {
		if (runningExperiment == null) {
			return;
		}
		runningExperiment.getStatusMessageList().add(statusMessage);
		if (statusMessage.getStatusInfo() != null && statusMessage.getStatusInfo() instanceof ProgressInfo) {
			runningExperiment.setLastProgressInfo((ProgressInfo) statusMessage.getStatusInfo());
		}

	}

	/**
	 * Checks for failed adding of {@link EventType.MEASUREMENT_FINISHED}. Fires this
	 * event manually with a {@link StatusMessage}.
	 * 
	 * Marked as TODO for Marius Öhler. Why is this method used?
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
	 */
	@Override
	public void onNewStatus(StatusMessage statusMessage) {
		LOGGER.info("New Status on '" + this.controllerURL + "': " + statusMessage.getEventType());
		
		nextStatusMessage(statusMessage);

		if (statusMessage.getEventType() == EventType.EXECUTION_FAILED) {
			LOGGER.warn("Experiment could not be executed succesfully. Status: Execution failed");
		}

		if (statusMessage.getEventType() == EventType.MEASUREMENT_FINISHED) {
			processFinishedExperiment();
		}
	}

	/**
	 * Ends the execution of the current experiment and stores information about
	 * it in the database.
	 */
	private synchronized void processFinishedExperiment() {
		LOGGER.info("Experiment id:" + runningExperiment.getScheduledExperiment().getId()
				    + " finished on: " + runningExperiment.getScheduledExperiment().getControllerUrl());

		runningExperiment.setTimeEnded(System.currentTimeMillis());
		
		saveDurationInExperiment();

		storeExecutedExperimentDetails();

		Configuration.removeConfiguration(experimentHashCode);

		executeStatus = null;
		runningExperiment = null;
		checkToExecuteNext();
	}

	/**
	 * Stores the final results for the exectured experiment in the database as
	 * an {@link ExecutedExperimentDetails} object. 
	 */
	private void storeExecutedExperimentDetails() {
		
		boolean hasError = false;
		
		for (StatusMessage sm : runningExperiment.getStatusMessageList()) {
			if (sm.getStatusInfo() != null && sm.getStatusInfo() instanceof ErrorInfo) {
				hasError = true;
				break;
			}
		}

		ExecutedExperimentDetails eed = new ExecutedExperimentDetails();
		// eed.setEventLog(runningExperiment.getEventLogLiteList()); // TODO why was commented out?

		eed.setSuccessful(!hasError);
		eed.setTimeFinished(runningExperiment.getTimeEnded());
		eed.setTimeStarted(runningExperiment.getTimeStarted());
		eed.setName(runningExperiment.getScheduledExperiment().getLabel());
		eed.setControllerURL(runningExperiment.getScheduledExperiment().getControllerUrl());

		eed.setAccountId(runningExperiment.getScheduledExperiment().getAccountId());
		eed.setScenarioName(runningExperiment.getScheduledExperiment().getScenarioDefinition().getScenarioName());

		long generatedId = ServicePersistenceProvider.getInstance().storeExecutedExperimentDetails(eed);

		MECLog log = new MECLog();
		log.setId(generatedId);
		log.setEntries(runningExperiment.getEventLogLiteList());

		ServicePersistenceProvider.getInstance().storeMECLog(log);
	}

	/**
	 * Creates the CurrentControllerExperiment object, that contains all
	 * necessary information about the current controller state.
	 * */
	public RunningControllerStatus createControllerStatusPackage() {
		if (runningExperiment == null) {
			return null;
		}
		RunningControllerStatus cce = new RunningControllerStatus();

		cce.setAccount(runningExperiment.getScheduledExperiment().getAccountId());
		cce.setScenario(runningExperiment.getScheduledExperiment().getScenarioDefinition().getScenarioName());
		cce.setTimeStart(runningExperiment.getTimeStarted());
		cce.setLabel(runningExperiment.getScheduledExperiment().getLabel());
		cce.setEventLogList(runningExperiment.getEventLogLiteList());

		if (runningExperiment.getStatusMessageList().get(runningExperiment.getStatusMessageList().size() - 1)
				.getEventType() == EventType.MEASUREMENT_FINISHED) {
			cce.setHasFinished(true);
		} else {
			cce.setHasFinished(false);
		}

		if (runningExperiment.getLastProgressInfo() != null) {
			ProgressInfo info = runningExperiment.getLastProgressInfo();
			float progress = 100F / info.getNumberOfRepetition() * info.getRepetition();
			cce.setProgress(progress);
		} else {
			cce.setProgress(-1);
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

}
