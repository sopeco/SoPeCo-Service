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
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectMapper;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * This is an example class to request the SoPeCo Service Layer and interact with it.<br />
 * If you need inspiration, I advise to peek at the org.sopeco.service.test.rest package.<br />
 * This example application:
 * <ol>
 * 		<li>Creates an account</li>
 * 		<li>Logs into the created account</li>
 * 		<li>Create an empty scenario</li>
 * 		<li>Fetch all the scenario for the current account</li>
 * 		<li>Fetch all the ExperimentSeriesDefinition for the current account</li>
 * </ol>
 * Please adjust all the class fields annotated with a TODO, to have the full joy at
 * interacting with the Service Layer.
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
	 * TODO: choose a name for your dummy scenario.
	 * Set the name for the dummy scenario which is created.
	 */
	private static final String scenarioName 	= "dummyScenario";

	/**
	 * TODO: choose a name for your dummy scenario.
	 * Set the name for the dummy scenario which is created.
	 */
	private static final String measurementSpecificationName = "dummyMS";

	/**
	 * TODO: choose a name for your dummy ExperimentSeriesDefinition.
	 * Set the name for the dummy ExperimentSeriesDefinition which is created.
	 */
	private static final String experimentSeriesDefinitionName = "dummyESD";
	
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
	 * @param args The program initialization arguments. They are dropped here.
	 */
    public static void main(String[] args) {
    	init();

    	createEmptyDummyScenario();
	
		fetchScenarioList();
		
		fetchExperimentSeriesDefinitionInfo();
    }

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// SINGLE SERVICE CALLS //////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * This method provides an example how to setup a more complex call to a servlet.
     * In addition, it's shown how to check the status of the response from the Service Layer
     * and how you can read simple entities from the response.
     */
	private static void createEmptyDummyScenario() {
		System.out.println("\n++++++++++++++++++++++++SCENARIO CREATION+++++++++++++++++++++++++++");
		
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_SCENARIO + "/"
											+ ServiceConfiguration.SVC_SCENARIO_ADD + "/"
											+ scenarioName);
		
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token);
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurementSpecificationName);

		// need to provide an ExperimentSeriesDefinition when creating a Scenario. However, it's possible to pass an
		// empty one. Please be aware to provide a valid ExperimentSeriesDefinition when executing a real scenario.
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		esd.setName(experimentSeriesDefinitionName);
    	Response r = webTarget.request(MediaType.APPLICATION_JSON).post(Entity.entity(esd, MediaType.APPLICATION_JSON));
    	
    	if (r.getStatus() == Status.OK.getStatusCode()) {
    		System.out.println("Succesfully added scenario with the name '" + scenarioName + "'.");
    	} else if (r.getStatus() == Status.CONFLICT.getStatusCode()) {
    		// a CONFLICT has always a message as entity in the whole Service Layer
    		// the message helps to identify the problem source
    		System.out.println(r.readEntity(String.class));
    	} else {
    		System.out.println("Scenario addition call failed with code: " + r.getStatus());
    	}
	}
    
	/**
     * This method provides an example how to fetch arrays.
     */
	private static void fetchScenarioList() {
		System.out.println("\n++++++++++++++++++++++++SCENARIO LISTING+++++++++++++++++++++++++++");
		
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_SCENARIO + "/"
											+ ServiceConfiguration.SVC_SCENARIO_LIST);
		
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token);

    	Response r = webTarget.request(MediaType.APPLICATION_JSON).get();

		System.out.println("Fetching all scenarios the current account has.");
		
    	if (r.getStatus() == Status.OK.getStatusCode()) {
        	String[] scenarioList = r.readEntity(String[].class);
        	
        	for (String scenario : scenarioList) {
        		System.out.println("Scenario name: " + scenario + "'");
        	}
    	}
	}
	
    /**
     * This method provides an example how to fetch generic types from the Service Layer.
     */
	private static void fetchExperimentSeriesDefinitionInfo() {
		System.out.println("\n++++++++++++++++++++++EXPERIMENT SERIES DEFINITION++++++++++++++++++++++++++");
		
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_ESD + "/"
											+ scenarioName + "/"
											+ measurementSpecificationName);
		
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token);
		
		Response r = webTarget.request(MediaType.APPLICATION_JSON).get();
		
		if (r.getStatus() == Status.OK.getStatusCode()) {
			// magic call, which converts the object passed with the reponse into a generic type
			List<ExperimentSeriesDefinition> ESDs = r.readEntity(new GenericType<List<ExperimentSeriesDefinition>>() { });
			
			for (ExperimentSeriesDefinition ESD : ESDs) {
				System.out.println("ESD name: '" + ESD.getName() + "'");
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
