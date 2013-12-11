package org.sopeco.service.rest;

import org.sopeco.service.shared.LoginData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("login")
public class Login {

	/**
	 * A unique access token for the beginning. Afterwarsd the accesstoken has to be
	 * gernerated automatically when a client first requests the service.
	 */
	private final static String ACCESS_TOKEN = "6A1337B7";
	
	/**
	 * Returns the client an accesstoken, when he wants to request the
	 * service. The client identifies via this accesstoken every time
	 * it requests a service (like registering a MEC).
	 * 
	 * @return Login data for the client in a {@link org.sopeco.service.shared.LoginData}
	 *  	   object.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public LoginData loginRequest() {
		
		// create the POJO to give the client a LoginData class
		LoginData loginData = new LoginData(ACCESS_TOKEN, true);
		return loginData;
 
	}
	
}
