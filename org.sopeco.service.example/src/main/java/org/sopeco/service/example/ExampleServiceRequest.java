package org.sopeco.service.example;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.exchange.ServiceResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

/**
 * This is an example class to request the SoPeCo Service Layer and interact with it.<br />
 * If you need inspiration, I advise to peek at the org.sopeco.test.rest package. All
 * the services are testes there for regression testing. The call to the service as such
 * is nearly the same and can be copied.<br />
 * <br />
 * This example program just creates and account and logs in. You might want to adjust the
 * accountname, as it is often already taken.
 * 
 * @author Peter Merkert
 */
public final class ExampleServiceRequest {

	/**
	 * TODO: choose a unique acccount name
	 */
	private static final String accountname 	= "myAccount";	
	
	/**
	 * TODO: choose a password
	 */
	private static final String password 		= "myPassword";
	
	/**
	 * As this is only an example program, everything is done in the main
	 * method.
	 * 
	 * @param args the args
	 */
    public static void main(String[] args) {

    	Client client = Client.create();
    	
    	// set values to try service
		String serviceUrl 	= "http://localhost:8080/";
		
		///////////////////////////////////// ACCOUNT CREATION ////////////////////////////////////////////
		
		// define the request to query
		WebResource webResource = client.resource(serviceUrl
												+ ServiceConfiguration.SVC_ACCOUNT + "/"
												+ ServiceConfiguration.SVC_ACCOUNT_CREATE);
		
		// define parameters to pass to the REST call (can be queryParam and pathParam)
		webResource = webResource.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
		webResource = webResource.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
		
		ServiceResponse<Boolean> sr_b = new ServiceResponse<Boolean>();
		
		try {
			
			// do the REST call
			sr_b = webResource.accept(MediaType.APPLICATION_JSON)
							  .post(new GenericType<ServiceResponse<Boolean>>() { });
		 
		} catch (Exception e) {
		 
			e.printStackTrace();
			System.out.println("Error occured calling web service!");
			return;
			
		}
		
		// check HTTP status of request
		if (sr_b.getStatus() == Status.OK) {
			
			System.out.println("Account created succesfully.");
			
		} else {
			
			System.out.println("Error in account creation.");
			return;
			
		}
		
		
		///////////////////////////////////// LOGGING IN ////////////////////////////////////////////
    	
    	// define the request to query
		webResource = client.resource(serviceUrl
								    + ServiceConfiguration.SVC_ACCOUNT + "/"
									+ ServiceConfiguration.SVC_ACCOUNT_LOGIN);
		
		// define parameters to pass to the REST call (can be queryParam and pathParam)
		webResource = webResource.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
		webResource = webResource.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
    	
		ServiceResponse<String> sr_s = new ServiceResponse<String>();
		
		try {
			
			// do the REST call
			sr_s = webResource.accept(MediaType.APPLICATION_JSON)
							  .get(new GenericType<ServiceResponse<String>>() { });
		 
		} catch (Exception e) {
		 
			e.printStackTrace();
			System.out.println("Error occured calling web service!");
			return;
			
		}

		// check HTTP status of request
		if (sr_s.getStatus() == Status.OK) {

			// when the call was successful, we got a token
			System.out.println("Token got for logging in and authentificate: " + sr_s.getObject());
			
		} else {
			
			System.out.println("Error in account creation.");
			return;
			
		}
	  
    }
    
    
}
