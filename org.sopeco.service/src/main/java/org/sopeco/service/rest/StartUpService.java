package org.sopeco.service.rest;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.socket.SocketAcception;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.execute.ExecutionScheduler;

/**
 * The <code>StartUpService</code> class is used to handle the initialization of Servlets.
 * This class is called once in the program lifetime for the initialization {@link
 * #onStartup(Container)} and once when shutting down {@link #onShutdown(Container)}.
 * <br />
 * <br />
 * One initialization is the ServerSocket startup for listening for MECs.
 * 
 * @author Peter Merkert
 */
@Provider
public final class StartUpService extends AbstractContainerLifecycleListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartUpService.class);
		
	/**
	 * Starts a ServerSocket via the <code>SocketAcception</code> to wait for
	 * <code>MeasurementEnvironmentController</code> connections.
	 * <br />
	 * This is a non-blocking method, because the ServerSocket is handled
	 * via the class <code>SocketAcception</code>.
	 */
	@Override
	public void onStartup(Container arg0) {

		LOGGER.debug("RESTful SoPeCo Service Layer starting up.");	
		
		try {
	    	// open the ServerSocket on the port set in the configuration
			SocketAcception.open(ServiceConfiguration.MEC_SOCKET_PORT);
		} catch (RuntimeException re) {
			LOGGER.warn("Port {} already in use.", ServiceConfiguration.MEC_SOCKET_PORT);
		}
		
		// start the experiment scheduler to peek for executable senarios
		ExecutionScheduler.getInstance().startScheduler();
		
	}

	/**
	 * Stops the {@link ExecutionScheduler}, which was started in the beginning.
	 */
	@Override
	public void onShutdown(Container arg0) {

		LOGGER.debug("RESTful SoPeCo Service Layer shutting down.");
		
		// start the experiment scheduler to peek for executable senarios
		while (!ExecutionScheduler.getInstance().stopScheduler()) {
			LOGGER.info("Shutdown of experiment scheduler failed. Try again.");
		}
		
	}
	
}
