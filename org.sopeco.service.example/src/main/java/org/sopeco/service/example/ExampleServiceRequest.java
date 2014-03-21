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

import org.glassfish.jersey.client.ClientConfig;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.AccountDetails;
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
	private static final String accountname 	= "test";	
	
	/**
	 * TODO: choose a password
	 */
	private static final String password 		= "test";
	
	/**
	 * TODO: choose the URL where the SoPeCo SL is running
	 */
	private static final String serviceUrl 		= "http://localhost:8080/";
	
	private static Client client;
	private static String token = "4dfd4535-ec87-48b9-9029-b805347d61e8";
	/**
	 * As this is only an example program, everything is done in the main
	 * method.
	 * 
	 * @param args the args
	 */
    public static void main(String[] args) {
    	init();
    	
    	WebTarget webTarget = client.target(serviceUrl
				+ ServiceConfiguration.SVC_ACCOUNT + "/"
				+ ServiceConfiguration.SVC_ACCOUNT_INFO);

    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token);
    	Response r = webTarget.request(MediaType.APPLICATION_JSON).get();
		AccountDetails accountDetails = r.readEntity(AccountDetails.class);
		System.out.println(accountDetails.toString());

		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
		
		for (String s : accountDetails.getScenarioNames()) {
			System.out.println("Szenarioname: " + s);
		}
		
		webTarget = client.target(serviceUrl
				+ ServiceConfiguration.SVC_SCENARIO + "/"
				+ ServiceConfiguration.SVC_SCENARIO_DEFINITON);
		
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token);
		
		ScenarioDefinition sd = webTarget.request(MediaType.APPLICATION_JSON).get().readEntity(ScenarioDefinition.class);
	
		System.out.println("Derzeitig ausgewählte ScenarioDeifinition (via REST): " + sd.getScenarioName());
    	
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
		
    	webTarget = client.target(serviceUrl
				+ ServiceConfiguration.SVC_MEASUREMENT + "/"
				+ ServiceConfiguration.SVC_MEASUREMENT_LISTSPECS);

    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token);
    	
    	r = webTarget.request(MediaType.APPLICATION_JSON).get();
    	
		List<MeasurementSpecification> list = r.readEntity(new GenericType<List<MeasurementSpecification>>() { });
		
		for (MeasurementSpecification ms : list) {

			System.out.println("MS name: " + ms.getName());
			
			List<ExperimentSeriesDefinition> listESD = ms.getExperimentSeriesDefinitions();
			
			for (ExperimentSeriesDefinition esd : listESD) {
				
				System.out.println("ESD name: " + esd.getName());
				
			}
			
		}
		
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
		
		webTarget = client.target(serviceUrl
				+ ServiceConfiguration.SVC_MEC + "/"
				+ ServiceConfiguration.SVC_MEC_MED);

    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token);
    	webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_MEC_URL, "socket://MatrixMultiplicationMECId/Testcontroller");
    	
    	r = webTarget.request(MediaType.APPLICATION_JSON).get();
    	
    	MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
    	
    	System.out.println(med.getRoot().getAllParameters().get(0).getFullName());
    	System.out.println(med.getRoot().getAllParameters().get(1).getFullName());
    	System.out.println(med.getRoot().getAllParameters().get(2).getFullName());
    	System.out.println(med.getRoot().getAllParameters().get(3).getFullName());
    }
	
	private static void init() {
		client = ClientBuilder.newClient(createClientConfig());
		//createAccount();
		//login();
	}

	private static void login() {
		WebTarget webTarget = client.target(serviceUrl
											+ ServiceConfiguration.SVC_ACCOUNT + "/"
											+ ServiceConfiguration.SVC_ACCOUNT_LOGIN);
	
		// define parameters to pass to the REST call (can be queryParam and pathParam)
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname);
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password);
		
		token = webTarget.request(MediaType.APPLICATION_JSON).get().readEntity(String.class);
		System.out.println("Token: " + token);
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
