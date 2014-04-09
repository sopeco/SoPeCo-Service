package org.sopeco.service.example;

import java.util.List;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectMapper;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * This is an example class to request the SoPeCo Service Layer and interact with it.<br />
 * If you need inspiration, I advise to peek at the org.sopeco.test.rest package.<br />
 * This example program just creates an account and logs into this. You might want to adjust the
 * accountname, as it is often already taken.
 * 
 * @author Peter Merkert
 */
public final class ExampleServiceRequest {

	/**
	 * TODO: choose a unique acccount name.
	 * As the accountname is often already taken, please choose a unique new one.
	 */
	private static final String accountname 	= "testaccount";	
	
	/**
	 * TODO: choose a password.
	 * Please set a password for your account.
	 */
	private static final String password 		= "testpassword";
	
	/**
	 * TODO: choose the URL where the SoPeCo SL is running.
	 * Set the URL where the Service Layer is running.
	 */
	private static final String serviceUrl 		= "http://localhost:8080/";
	
	/**
	 * The token is fetched when loggin in and used for authentication
	 * when doing a call to the Service Layer.
	 */
	private static String token 				= "";

	/**
	 * The {@link Client} is used to build up the service calls. See example below
	 * to see the handling with the {@link Client}.
	 */
	private static Client client;
	
	/**
	 * As this is only an example program, everything is done in the main
	 * method.
	 * 
	 * @param args the args
	 */
    public static void main(String[] args) {
    	init();

		fetchGeneralInfo();
	
		fetchScenarioResults();
    }

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// SINGLE SERVICE CALLS //////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 
     */
	private static void fetchGeneralInfo() {
		System.out.println("\n+++++++++++++++++++++++GENERAL INFO+++++++++++++++++++++++++++");
		
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_SCENARIO_LIST);
		
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token);
		
		Response r = webTarget.request(MediaType.APPLICATION_JSON).get();
		
		if (r.getStatus() == Status.OK.getStatusCode()) {
			String[] scenarioNames = r.readEntity(String[].class);
			
			for (String sn : scenarioNames) {
				System.out.println("Scenario name: " + sn);
			}
		}
	}

    /**
     * This method provides an example to fetch generic types from the Service Layer.
     */
	private static void fetchScenarioResults() {
		System.out.println("\n+++++++++++++++++++++++++RESULT++++++++++++++++++++++++++++");
		
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_SCENARIO + "/"
											+ ServiceConfiguration.SVC_SCENARIO_INSTANCES);
		
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token);
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "meneSzenario");

    	Response r = webTarget.request(MediaType.APPLICATION_JSON).get();
    	
    	if (r.getStatus() == Status.OK.getStatusCode()) {
        	List<ScenarioInstance> scenarioList = r.readEntity(new GenericType<List<ScenarioInstance>>() { });
        	
        	if (scenarioList.size() > 0) {
        		System.out.println("At least one ScenarioInstance could be found.");
            	System.out.println("1st ScenarioInstance name: " + scenarioList.get(0).getName());
            	System.out.println("1st ScenarioInstance MEC URL: " + scenarioList.get(0).getMeasurementEnvironmentUrl());
        	}
    	}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// INITIALIZATION STUFF //////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void init() {
		client = ClientBuilder.newClient(createClientConfig());
		createAccount();
		login();
	}

	private static void login() {
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_ACCOUNT + "/"
											+ ServiceConfiguration.SVC_ACCOUNT_LOGIN);
	
		// define parameters to pass to the REST call (can be queryParam and pathParam)
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
		
		Response r = webTarget.request(MediaType.APPLICATION_JSON).get();
		
		if (r.getStatus() == Status.OK.getStatusCode()) {
			token = r.readEntity(String.class);
			System.out.println("Token fetched after logging in: " + token);
		} else {
			System.out.println("Cannot log in. Please choose another username and password.");
			System.exit(0);
		}
	}

	private static void createAccount() {
		// define the request to query
    	WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_ACCOUNT + "/"
											+ ServiceConfiguration.SVC_ACCOUNT_CREATE);
		
		// define parameters to pass to the REST call (can be queryParam and pathParam)
    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
    	
    	// Null.class, cause Jersey expects an object passed with HTTP POST
		webTarget.request(MediaType.APPLICATION_JSON).post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
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
