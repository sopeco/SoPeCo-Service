package org.sopeco.service.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.ext.Provider;

import org.sopeco.config.Configuration;
import org.sopeco.engine.measurementenvironment.socket.SocketAcception;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * The {@Code StartUpService} class is used to handle the initialization of Servlets.
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

	/**
	 * Starts a ServerSocket via the {@code SocketAcception} to wait for
	 * MEController connection.
	 */
	@Override
    public void contextInitialized(ServletContextEvent sce) {
    	
    	// open the ServerSocket on the port set in the configuration
		SocketAcception.open(ServiceConfiguration.MEC_SOCKET_PORT);
		
    }

	@Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
    
}
