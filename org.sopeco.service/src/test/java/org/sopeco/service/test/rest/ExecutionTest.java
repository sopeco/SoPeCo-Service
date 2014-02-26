package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.exchange.ExperimentStatus;
import org.sopeco.service.rest.json.CustomObjectMapper;
import org.sopeco.service.test.configuration.TestConfiguration;
import org.sopeco.service.test.rest.fake.TestMEC;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * The <code>ExecutionTest</code> tests a whole execution run. From login via scenario creation
 * to MEC registration and test execution.
 * 
 * @author Peter Merkert
 */
public class ExecutionTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionTest.class.getName());
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public ExecutionTest() {
		super();
	}
	
	/**
	 * This method is called on the Grizzly container creation of a {@link JerseyTest}.
	 * It's used to configure where the servlet container.<br />
	 * In this case, the package is definied where the RESTful services are and
	 * the {@link CustomObjectMapper} is registered.
	 */
	@Override
    protected Application configure() {
		ResourceConfig rc = new ResourceConfig();
		rc.packages(TestConfiguration.PACKAGE_NAME_REST);
		
		// the CustomObjectMapper must be wrapped into a Jackson Json Provider
		// otherwise Jersey does not recognize to use Jackson for JSON
		// converting
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());
		rc.register(provider);
		
		return rc;
    }

	/**
	 * The {@link Client} needs also the {@link CustomObjectMapper}, which
	 * defines the mixin used when the objects were serialized.
	 */
	@Override
	protected void configureClient(ClientConfig config) {
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());
        config.register(provider);
	}
	
	/**
	 * Cleans up the database means: Delete the scenario with name {@code 
	 * MeasurementControllerServiceTest.SCENARIO_NAME} in the database. This scenario
	 * is used by every single test and it can cause errors, when the scenario is created,
	 * but already in the database. Because the database instance is then not updated,
	 * which can result in unexpected behaviour.
	 */
	@After
	public void cleanUpDatabase() {
		LOGGER.debug("Cleaning up the database.");
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String scenarioNameEmpty = TestConfiguration.TEST_CLEAN_SCENARIO_NAME;
		String measSpecNameEmpty = TestConfiguration.TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
					      	 .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);

		// clean the scheduling list for the user
		target().path(ServiceConfiguration.SVC_EXECUTE)
				.path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				.request(MediaType.APPLICATION_JSON)
				.delete();
	
		// now create empty scenario to delete the test scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
				.path(ServiceConfiguration.SVC_SCENARIO_ADD)
				.path(scenarioNameEmpty)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measSpecNameEmpty)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		target().path(ServiceConfiguration.SVC_SCENARIO)
				.path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				.path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioNameEmpty)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// delete the example scenario
		target().path(ServiceConfiguration.SVC_SCENARIO)
				.path(TestConfiguration.TEST_SCENARIO_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			    .request(MediaType.APPLICATION_JSON)
			    .delete();
	}
	
	/**
	 * Tests a whole execution run.
	 * 
	 * 1. login
	 * 2. get the account related to the current user
	 * 3. add scenario
	 * 4. switch scenario
	 * 5. get current scenario definition
	 * 6. switch measurement specification
	 * 7. start the TestMEC
	 * 8. add a ScheduledExperiment with controller information to service
	 * 9. get ID of ScheduledExperiment from step 8
	 * 10. execute the ScheduledeExperiment with ID from step 9
	 */
	@Test
	public void testExecution() {
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
					      	 .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);

		// account is needed for account id
		r = target().path(ServiceConfiguration.SVC_ACCOUNT)
				    .path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
				    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
			      	.request(MediaType.APPLICATION_JSON)
				    .get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		Account account = r.readEntity(Account.class);
		
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		esd.setName("experimentSeriesDefintion");
		target().path(ServiceConfiguration.SVC_SCENARIO)
			    .path(ServiceConfiguration.SVC_SCENARIO_ADD)
			    .path(TestConfiguration.TEST_SCENARIO_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			    .request(MediaType.APPLICATION_JSON)
			    .post(Entity.entity(esd, MediaType.APPLICATION_JSON));

		target().path(ServiceConfiguration.SVC_SCENARIO)
			    .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
			    .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TestConfiguration.TEST_SCENARIO_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
		      	.request(MediaType.APPLICATION_JSON)
		      	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		r = target().path(ServiceConfiguration.SVC_SCENARIO)
				    .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
				    .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		ScenarioDefinition sd = r.readEntity(ScenarioDefinition.class);
		
		assertEquals(true, sd != null); // the user must have a scenario now
		
		//switch to created measurement specification
		r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
			        .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
			        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME)
			        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			      	.request(MediaType.APPLICATION_JSON)
			      	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		boolean repeating 		= false;
		String controllerURL 	= "socket://" + TestMEC.MEC_ID + "/" + TestMEC.MEC_SUB_ID_1;
		String label 			= "myScheduledExperiment";
		long accountId 			= account.getId();
		boolean scenarioActive 	= false;
		long addedTime 			= System.currentTimeMillis();
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		se.setActive(scenarioActive);
		se.setAddedTime(addedTime);
		
		// add to execution list
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
			        .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
			        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      	.request(MediaType.APPLICATION_JSON)
			      	.post(Entity.entity(se, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// get id for the added scenario
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
			        .path(ServiceConfiguration.SVC_EXECUTE_ID)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      	.request(MediaType.APPLICATION_JSON)
			      	.put(Entity.entity(se, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		String id = String.valueOf(r.readEntity(Long.class));

		// now select experiments to execute
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
					.path(id)
					.path(ServiceConfiguration.SVC_EXECUTE_ESD)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .queryParam(ServiceConfiguration.SVCP_EXECUTE_EXPERIMENTSERIES, "experimentSeriesDefintion")
			      	.request(MediaType.APPLICATION_JSON)
			      	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// now get the database entry for the ScheduledExperiment
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
	       	        .path(id)
	       	        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      	.request(MediaType.APPLICATION_JSON)
	       	        .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		ScheduledExperiment se2 = r.readEntity(ScheduledExperiment.class);
		
		assertEquals("experimentSeriesDefintion", se2.getSelectedExperiments().get(0));
		
		// then simply activate the execution
		r =  target().path(ServiceConfiguration.SVC_EXECUTE)
		       	     .path(id)
		       	     .path(ServiceConfiguration.SVC_EXECUTE_ENABLE)
		       	     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      	 .request(MediaType.APPLICATION_JSON)
			      	 .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		int key = r.readEntity(Integer.class);
		
		assertEquals(se2.getExperimentKey(), key);
		assertEquals(true, se.getExperimentKey() != key);
		
		String experimentKey = String.valueOf(key);
		
		r =  target().path(ServiceConfiguration.SVC_EXECUTE)
		       	     .path(ServiceConfiguration.SVC_EXECUTE_STATUS)
		       	     .queryParam(ServiceConfiguration.SVCP_EXECUTE_KEY, experimentKey)
		       	     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				     .request(MediaType.APPLICATION_JSON)
		       	     .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		ExperimentStatus es = r.readEntity(ExperimentStatus.class);
		
		assertEquals(TestConfiguration.TEST_SCENARIO_NAME, es.getScenarioName());
	}
	
}
