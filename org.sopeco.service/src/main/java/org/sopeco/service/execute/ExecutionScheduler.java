/**
 * Copyright (c) 2014 SAP
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.exchange.ExperimentStatus;

/**
 * The singleton class provides a scheduler for the experiments. The class mainly enables
 * to enqueue new experiments.<br />
 * This is a thread and is called periodically via a {@link ScheduledExecutorService} time
 * scheduler. Then all the execution queues are checked for experiments which can be
 * executed.<br />
 * 
 * @author Peter Merkert
 */
public final class ExecutionScheduler implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionScheduler.class);
	
	/**
	 * The singleton variable of the class.
	 */
	private static ExecutionScheduler experimentScheduler;
	
	/**
	 * Java has a {@link ScheduledExecutorService}, which provides a method to
	 * run a thread on fixed rates. This is used to dispatch this class
	 * every few seconds automatically.
	 */
	private static ScheduledExecutorService scheduler;
	
	/**
	 * Stores whether the execution scheduler has started. Need to be 
	 * stored as this is a singleton and can only be started once.
	 */
	private static boolean isStarted = false;
	
	private ExecutionScheduler() {
	}
	
	/**
	 * Singleton get method.
	 * 
	 * @return the singleton for the <code>ExperimentScheduler</code>
	 */
	public static ExecutionScheduler getInstance() {
		if (experimentScheduler == null) {
			experimentScheduler = new ExecutionScheduler();
		}
		
		return experimentScheduler;
	}
	
	/**
	 * Starts the scheduler service.
	 */
	public void startScheduler() {
		
		if (!isStarted) {
			LOGGER.info("Starting scheduling thread.");
			scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(this,
										ServiceConfiguration.SCHEDULING_REPEATE_INTERVAL, 
										ServiceConfiguration.SCHEDULING_REPEATE_INTERVAL,
										ServiceConfiguration.SCHEDULING_TIME_UNIT);
		
			isStarted = true;
		}
		
	}
	
	/**
	 * Stops the scheduler service.
	 * 
	 * @return true, if the scheduler can be stopped. False if there was an error, or the
	 * 		   scheduler has not been started yet.
	 */
	public boolean stopScheduler() {
		
		if (isStarted) {
			scheduler.shutdown();
			
			try {
				scheduler.awaitTermination(ServiceConfiguration.SCHEDULING_TIMEOUT_INTERVAL,
										   ServiceConfiguration.SCHEDULING_TIME_UNIT);
			} catch (InterruptedException e) {

				LOGGER.warn("The scheduler cannot shutdown normally, because threads are still running. Please try it again.");
				return false;
				
			}
			
			scheduler = null;
			isStarted = false;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * This method does a <b>ONE TIME</b> check for experiments which can be executed.
	 * <br />
	 * If you want to have this method executed in an interval, please call <code>startScheduler()</code>.
	 */
	@Override
	public void run() {
		
		// fetch the schedulede experiment list and execute ready ones
		LOGGER.debug("Checking for scheduled experiments");
		try {
			List<ScheduledExperiment> experimentList = ServicePersistenceProvider.getInstance().loadAllScheduledExperiments();

			for (ExecutionQueue queue : ExecutionQueueManager.getAllQueues()) {
				queue.check();
			}

			for (ScheduledExperiment experiment : experimentList) {
				if (experiment.getNextExecutionTime() < System.currentTimeMillis() && experiment.isActive()) {
					// Experiment will be executed
					enqueueExperiment(experiment);
				} else if (experiment.getNextExecutionTime() < System.currentTimeMillis() && experiment.isRepeating()) {
					// Calculates the next execution time.
					updateNextExecutionTime(experiment);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
		
	}
	
	/**
	 * Inserts an experiment into the execution queue. If the experiment has been added to
	 * the execution queue <b>it will be deleted as {@link ScheduledExperiment} out of the database</b>!<br />
	 * However, the experiment status can be queried via the return key of this method.
	 * 
	 * @param experiment 	the experiment to enqueue to the experiment queue
	 * @return 				the hashcode to access the added experiment afterwards
	 */
	private String enqueueExperiment(ScheduledExperiment experiment) {
		
		LOGGER.info("Insert experiment '" + experiment.getLabel()
					+ "' (id: " + experiment.getId()
					+ " - account: " + experiment.getAccountId()
					+ ") in queue.");

		ExecutionQueueManager.get(experiment.getControllerUrl()).addExperiment(experiment.createQueuedExperiment());

		if (experiment.isRepeating()) {
			
			LOGGER.info("Update execution times for the experiment with hashcode '{}'", experiment.getExperimentKey());
			experiment.setLastExecutionTime(System.currentTimeMillis());
			updateNextExecutionTime(experiment);
			
		} else {
			
			LOGGER.info("Remove ScheduleExperiment");
			ServicePersistenceProvider.getInstance().removeScheduledExperiment(experiment);
			
		}

		return String.valueOf(experiment.getExperimentKey());
		
	}
	
	/**
	 * Fetches the status of the experiment with the given key.
	 * 
	 * @param experimentKey	the key to identify the experiment
	 * @return				{@link ExperimentStatus} of the experiment with the given key, null if
	 * 						the key does not match
	 */
	public ExperimentStatus getExperimentStatus(long experimentKey) {
		
		LOGGER.info("Trying to fetch experiment with key: " + experimentKey);
		
		// this can be a bottleneck, when a lot of experiments are enqueued
		for (ExecutionQueue queue : ExecutionQueueManager.getAllQueues()) {
			
			ExperimentStatus status = queue.getExperimentStatus(experimentKey);

			if (status != null) {
				return status;
			}
		
		}
		
		// now check the ScheduledExperiment database for the experiment
		// the experiment can be in the list of ScheduledExperiments waiting in active state to be scheduled
		for (ScheduledExperiment se : ServicePersistenceProvider.getInstance().loadAllScheduledExperiments()) {	
			if (se.getExperimentKey() == experimentKey) {
				return createExperimentStatus(se);
			}	
		}
		
		// now check the ExecutedExperimentDetails
		// the experiment is then already finished
		ExecutedExperimentDetails eed = ServicePersistenceProvider.getInstance().loadExecutedExperimentDetails(experimentKey);
		
		if (eed != null) {
			return createExperimentStatus(experimentKey, eed);
		}
		
		// the experiment with given key cannot be found anywhere in the database
		return null ;
	}
	
	/**
	 * Sets the status of the experiment with the given key to abort. The information
	 * is passed via a configuration property. Therfor the aborting may take a little time
	 * to be processed.
	 * 
	 * @param experimentKey	the experiment key of the experiment to abort
	 */
	public void setExperimentAborting(long experimentKey) {
		
		for (ExecutionQueue queue : ExecutionQueueManager.getAllQueues()) {
			
			ExperimentStatus status = queue.getExperimentStatus(experimentKey);

			if (status != null) {
				
				queue.abortExperiment(experimentKey);
				
			}
			
		}
	}
	
	/**
	 * Updates the experiments times for repeating experiments. The execution time
	 * for the experiment is calculacted and stored in the database.
	 * 
	 * @param experiment the experiment to check the next execution time
	 */
	private void updateNextExecutionTime(ScheduledExperiment experiment) {
		long nextRepetition = ScheduleExpression.nextValidDate(experiment.getRepeatDays(),
															   experiment.getRepeatHours(),
															   experiment.getRepeatMinutes());
		experiment.setNextExecutionTime(nextRepetition);
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(experiment);
	}

	/**
	 * Creates the {@link ExperimentStatus} for an experiment with the given key.
	 * 
	 * @param se	the {@link ScheduledExperiment}
	 * @return		the {@link ExperimentStatus}
	 */
	private ExperimentStatus createExperimentStatus(ScheduledExperiment se) {
		ExperimentStatus status = new ExperimentStatus();
		status.setAccountId(se.getAccountId());
		status.setEventLogList(new ArrayList<MECLogEntry>());
		status.setFinished(false);
		status.setLabel(se.getLabel());
		status.setProgress(0.0f);
		status.setScenarioName(se.getScenarioDefinition().getScenarioName());
		status.setTimeStart(se.getStartTime());
		status.setTimeRemaining(0);
		
		return status;
	}

	/**
	 * Creates the {@link ExperimentStatus} with the passed information. Information
	 * about the {@link MECLogEntry}s is fetched from the database via the experimentkey.
	 * 
	 * @param experimentKey	the experiment key
	 * @param eed			the {@link ExecutedExperimentDetails}
	 * @return				the {@link ExperimentStatus}
	 */
	private ExperimentStatus createExperimentStatus(long experimentKey, ExecutedExperimentDetails eed) {
		
		LOGGER.debug("+++++++++++++++++++++++++++++++++++++++");
		LOGGER.debug("" + experimentKey);
		
		ExperimentStatus status = new ExperimentStatus();
		status.setAccountId(eed.getAccountId());
		status.setEventLogList(new ArrayList<MECLogEntry>());
		status.setFinished(true);
		status.setLabel(eed.getName());
		status.setProgress(100.0f);
		status.setScenarioName(eed.getScenarioName());
		status.setTimeStart(eed.getTimeStarted());
		status.setTimeRemaining(0);
		
		// the event log list
		List<MECLogEntry> mecloglist = new ArrayList<MECLogEntry>();
		
		MECLog meclog = ServicePersistenceProvider.getInstance().loadMECLog(experimentKey);

		LOGGER.debug("searching MECLOG");
		
		if (meclog != null) {
			LOGGER.debug("found MECLOG");
			mecloglist = meclog.getEntries();
		}
		
		status.setEventLogList(mecloglist);
		return status;
	}
	
}
