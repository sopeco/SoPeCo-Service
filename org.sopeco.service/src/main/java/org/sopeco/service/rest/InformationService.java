package org.sopeco.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sopeco.config.Configuration;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * The <code>InformationService</code> provides simple information about the SoPeCo service layer.
 * When requested with a browsers, the default <code>getInformation()</code> method is called.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_INFO)
public class InformationService {

	/**
	 * Prints information about the SoPeCo Service.
	 * 
	 * @return information text about this service
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getInformation() {

		PersistenceConfiguration conf = PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());
		
		return "<h1>Information about SoPeCo Service</h1>"
				+ "<p>Services can be requested.</p>"
				+ "<h2>Configuration information</h2>"
				+ "Metadata database: " + conf.getMetaDataHost() + ":" + conf.getMetaDataPort();
	}
	
	/**
	 * Returns true, if the service is running.
	 * 
	 * @return true, if the service is running
	 */
	@GET
	@Path(ServiceConfiguration.SVC_INFO_RUNNING)
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean running() {
		return true;
	}
	
}
