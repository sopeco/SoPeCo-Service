package org.sopeco.service.example;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectMapper;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

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
	 * TODO: choose the URL where the SoPeCo SL is running
	 */
	private static final String serviceUrl 		= "http://localhost:8080/";
	
	/**
	 * As this is only an example program, everything is done in the main
	 * method.
	 * 
	 * @param args the args
	 */
    public static void main(String[] args) {

    	Client client = ClientBuilder.newClient(createClientConfig());
		
		///////////////////////////////////// ACCOUNT CREATION ////////////////////////////////////////////
		
		// define the request to query
    	WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_ACCOUNT + "/"
											+ ServiceConfiguration.SVC_ACCOUNT_CREATE);
		
		// define parameters to pass to the REST call (can be queryParam and pathParam)
    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
    	
    	// Null.class, cause Jersey expects an object passed with HTTP POST
		Response r = webTarget.request(MediaType.APPLICATION_JSON)
				  			  .post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
 
		// check HTTP status of request
		if (r.getStatus() == Status.OK.getStatusCode()) {
			
			System.out.println("Account created succesfully.");
			
		} else {
			
			System.out.println("Error in account creation.");
			return;
			
		}
		
		
		///////////////////////////////////// LOGGING IN ////////////////////////////////////////////
    	
    	// define the request to query
		webTarget = client.target(serviceUrl
								  + ServiceConfiguration.SVC_ACCOUNT + "/"
								  + ServiceConfiguration.SVC_ACCOUNT_LOGIN);
		
		// define parameters to pass to the REST call (can be queryParam and pathParam)
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
    	
		
		r = webTarget.request(MediaType.APPLICATION_JSON).get();

		// check HTTP status of request
		if (r.getStatus() == Status.OK.getStatusCode()) {

			// when the call was successful, we got a token
			System.out.println("Token got for logging in and authentificate: " + r.readEntity(String.class));
			
		} else {
			
			System.out.println("Error in account creation.");
			return;
			
		}
	  
    }
	
	/**
	 * Sets the client config for the client. The method adds a special {@link CustomObjectWrapper}
	 * to the normal Jackson wrapper for JSON.<br />
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new ClientConfig();
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());
        config.register(provider);
	    return config;
	}
    
    
}
