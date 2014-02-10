package org.sopeco.service.execute;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;

public final class ExperimentScheduler implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentScheduler.class);
	
	private static ExperimentScheduler experimentScheduler;
	
	private static ScheduledExecutorService scheduler;
	
	private static boolean isStarted = false;
	
	private ExperimentScheduler() {
	}
	
	/**
	 * Singleton get method.
	 * 
	 * @return the singleton for the <code>ExperimentScheduler</code>
	 */
	public static ExperimentScheduler getInstance() {
		if (experimentScheduler == null) {
			experimentScheduler = new ExperimentScheduler();
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
		
		// fetch the ScenarioInExecution list and execute scenarios
		
	}
	
}
