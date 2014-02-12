package org.sopeco.service.execute;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.ScheduledExperiment;

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
	 * the execution queue <b>it will be deleted as {@link ScheduledExperiment} out of the database</b>!
	 * 
	 * @param experiment the experiment to enqueue to the experiment queue
	 */
	private void enqueueExperiment(ScheduledExperiment experiment) {
		LOGGER.info("Insert experiment '" + experiment.getLabel()
					+ "' (id: " + experiment.getId()
					+ " - account: " + experiment.getAccountId()
					+ ") in queue.");

		ExecutionQueueManager.get(experiment.getControllerUrl()).addExperiment(experiment.createQueuedExperiment());

		if (experiment.isRepeating()) {
			
			LOGGER.info("Update execution times for the experiment with hashcode '{}'", experiment.hashCode());
			experiment.setLastExecutionTime(System.currentTimeMillis());
			updateNextExecutionTime(experiment);
			
		} else {
			
			LOGGER.info("Remove ScheduleExperiment");
			ServicePersistenceProvider.getInstance().removeScheduledExperiment(experiment);
			
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
	
}
