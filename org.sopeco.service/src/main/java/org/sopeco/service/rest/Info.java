package org.sopeco.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sopeco.service.configuration.ServiceConfiguration;

@Path(ServiceConfiguration.SVC_INFO)
public class Info {

	/**
	 * Prints information about the SoPeCo Service
	 * 
	 * @return information text about this service
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String createAccount() {
		return "<h1>Information about SoPeCo Service</h1>"
				+ "Services can be requested.";
	}
	
	/**
	 * Returns true, if the service is running.
	 * 
	 * @return true, if the service is running
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean running() {
		return true;
	}
	
}
