package org.sopeco.service.configuration;

import java.net.ServerSocket;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.socket.SocketAcception;
import org.sopeco.service.execute.ExecutionScheduler;

/**
 * The {@link ServletContainerLifecycleListener} class is used to handle the initialization of (servlet-){@link Container}.
 * This class is called once in the program lifetime of a <code>Container</code> for the {@link
 * #onStartup(Container)} and once when the <code>Container</code> is {@link #onShutdown(Container)}.
 * <br />
 * <br />
 * The main initialization is the ServerSocket listening for MECs.
 * 
 * @author Peter Merkert
 */
@Provider
public final class ServletContainerLifecycleListener implements ContainerLifecycleListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServletContainerLifecycleListener.class);
		
	/**
	 * Starts a ServerSocket via the {@link SocketAcception} to wait for
	 * <code>MeasurementEnvironmentController</code>s to connect.
	 * <br />
	 * This is a non-blocking method, because the {@link ServerSocket} is handled
	 * via the class {@link SocketAcception}, which itself is calling a thread.
	 */
	@Override
	public void onStartup(Container container) {

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
	 * Stops the {@link ExecutionScheduler}, which was started in the {@link #onStartup(Container)} call
	 * for this {@link Container}.
	 */
	@Override
	public void onShutdown(Container container) {

		LOGGER.debug("RESTful SoPeCo Service Layer shutting down.");
		
		// start the experiment scheduler to peek for executable senarios
		while (!ExecutionScheduler.getInstance().stopScheduler()) {
			LOGGER.info("Shutdown of experiment scheduler failed. Try again.");
		}
		
	}

	/**
	 * This method is empty overwritten.
	 */
	@Override
	public void onReload(Container container) {
	}
	
}
