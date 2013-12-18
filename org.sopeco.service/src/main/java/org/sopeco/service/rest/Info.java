package org.sopeco.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;

@Path(ServiceConfiguration.SVC_INFO)
public class Info {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Info.class);

	/**
	 * Prints information about the SoPeCo Service
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String createAccount() {
		return "<h1>Information about SoPeCo Service</h1>"
				+ "test";
	}
	
}
