package org.sopeco.service.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.socket.SocketAcception;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.execute.ExperimentScheduler;

/**
 * The <code>StartUpService</code> class is used to handle the initialization of Servlets.
 * This class is called once in the program lifetime for the initialization ({@code
 * contextInitialized}) and once when shutting down ({@code contextDestroyed}).
 * <br />
 * <br />
 * One initialization is the ServerSocket startup for listening for MEControllers.
 * 
 * @author Peter Merkert
 */
@Provider
public final class StartUpService implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartUpService.class);
	
	/**
	 * Starts a ServerSocket via the <code>SocketAcception</code> to wait for
	 * <code>MeasurementEnvironmentController</code> connections.
	 * <br />
	 * This is a non-blocking method, because the ServerSocket is handled
	 * via the class <code>SocketAcception</code>.
	 * 
	 * @param sce	the {@link ServletContextEvent}
	 */
	@Override
    public void contextInitialized(ServletContextEvent sce) {
    	
		LOGGER.debug("RESTful SoPeCo Service Layer starting up.");	
		
		try {
	    	// open the ServerSocket on the port set in the configuration
			SocketAcception.open(ServiceConfiguration.MEC_SOCKET_PORT);
		} catch (RuntimeException re) {
			LOGGER.warn("Port {} already in use.", ServiceConfiguration.MEC_SOCKET_PORT);
		}
		
		// start the experiment scheduler to peek for executable senarios
		ExperimentScheduler.getInstance().startScheduler();
		
    }

	/**
	 * Currently this method is empty overwritten.
	 * 
	 * @param sce	the {@link ServletContextEvent}
	 */
	@Override
    public void contextDestroyed(ServletContextEvent sce) {
		LOGGER.debug("RESTful SoPeCo Service Layer shutting down.");
		
		// start the experiment scheduler to peek for executable senarios
		while (!ExperimentScheduler.getInstance().stopScheduler()) {
			LOGGER.info("Shutdown of experiment scheduler failed. Try again.");
		}
		
    }
    
}
