package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
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
import org.sopeco.service.rest.ExecutionService;
import org.sopeco.service.rest.json.CustomObjectMapper;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * The {@link ExecutionServiceTest} tests various features of the
 * {@link ExecutionService} RESTful services.
 * 
 * @author Peter Merkert
 */
public class ExecutionServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionServiceTest.class);
	
	/**
	 * The TEST_SCENARIO_NAME is used to shorten the access to the test variable for the scenario name.
	 */
	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	
	/**
	 * The TEST_MEASUREMENT_SPECIFICATION_NAME is used to shorten the access to the test variable meas. spec. name.
	 */
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public ExecutionServiceTest() {
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
	 * Cleans up the database means: Delete the scenario with name {@link 
	 * MeasurementControllerServiceTest.SCENARIO_NAME} in the database. This scenario
	 * is used by every single test and it can cause errors, when the scenario is created,
	 * but already in the database. Because the database instance is then not updated,
	 * which can result in unexpected behaviour.
	 * <br />
	 * In addition all the scheduled scenarios for the test account are deleted.
	 */
	@After
	public void cleanUpDatabase() {
		LOGGER.debug("Cleaning up the database.");
		
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String scenarioNameEmpty = TestConfiguration.TEST_CLEAN_SCENARIO_NAME;
		String measSpecNameEmpty = TestConfiguration.TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
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
	 * Tests to delete a scheduled experiment.
	 * 
	 * 1. login
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. get current scenario definition
	 * 5. add scheduled experiment
	 * 6. delete schedulede experiment
	 * 7. check list of scheduled experiments
	 */
	@Test
	public void testScheduleExperimentDeletion() {
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
						     .get();
						
		String token = r.readEntity(String.class);
		
		r = target().path(ServiceConfiguration.SVC_ACCOUNT)
					.path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
					.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
					.request(MediaType.APPLICATION_JSON)
					.get();
		
		Account account = r.readEntity(Account.class);
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
				.path(ServiceConfiguration.SVC_SCENARIO_ADD)
				.path(TEST_SCENARIO_NAME)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(esd, MediaType.APPLICATION_JSON));

		target().path(ServiceConfiguration.SVC_SCENARIO)
			    .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
			    .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
			    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		r = target().path(ServiceConfiguration.SVC_SCENARIO)
					.path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
					.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					.request(MediaType.APPLICATION_JSON)
					.get();

		ScenarioDefinition sd = r.readEntity(ScenarioDefinition.class);
		
		assertEquals(true, sd != null); // the user must have a scenario now
		
		boolean repeating 		= false;
		String controllerURL 	= "myCustomURL";
		String label 			= "myScheduledExperiment";
		long accountId 			= account.getId();
		
		ScheduledExperiment se 	= new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
				    .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
				    .post(Entity.entity(se, MediaType.APPLICATION_JSON));

		assertEquals(true, Status.OK.getStatusCode() == r.getStatus());
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
				    .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
				    .delete();
	
		assertEquals(true, Status.OK.getStatusCode() == r.getStatus());
		
		// now try to get the scheduled experiment (which must be empty)
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
			        .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
			        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
			        .get();
			   
		List<ScheduledExperiment> list = r.readEntity(new GenericType<List<ScheduledExperiment>>() { });
	
		assertEquals(false, list == null);
		assertEquals(true, list.isEmpty());
	}
	
	/**
	 * Adds a {@code ScheduledExperiment} to the scheduler. The list of scheduled experiments
	 * is then requested and checked for the added one.
	 * 
	 * 1. login
	 * 2. get account related to user
	 * 3. add scenario
	 * 4. switch scenario
	 * 5. get current scenario definition
	 * 6. add scheduled experiment
	 * 7. get ID of scheduled experiment
	 * 8. get list of schedulede experiments
	 */
	@Test
	public void testScheduleExperiment() {
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		/*
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();

		// account is needed for account id
		ServiceResponse<Account> sr_account = resource().path(ServiceConfiguration.SVC_ACCOUNT)
													    .path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
													    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
													    .accept(MediaType.APPLICATION_JSON)
													    .get(new GenericType<ServiceResponse<Account>>() { });
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });

		ServiceResponse<ScenarioDefinition> sr_sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
															  .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
															  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
															  .type(MediaType.APPLICATION_JSON)
															  .get(new GenericType<ServiceResponse<ScenarioDefinition>>() { });

		assertEquals(true, sr_sd.getObject() != null); // the user must have a scenario now
		
		boolean repeating = false;
		String controllerURL = "myCustomURL";
		String label = "myScheduledExperiment";
		long accountId = sr_account.getObject().getId();
		boolean active = false;
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sr_sd.getObject());
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		se.setActive(active);
		
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_EXECUTE)
												  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
												  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
												  .accept(MediaType.APPLICATION_JSON)
												  .type(MediaType.APPLICATION_JSON)
												  .post(new GenericType<ServiceResponse<Boolean>>() { }, se);

		assertEquals(true, sr_b.getObject());

		// get the id of the schedulede experiment
		ServiceResponse<Long> sr_id = resource().path(ServiceConfiguration.SVC_EXECUTE)
											    .path(ServiceConfiguration.SVC_EXECUTE_ID)
											    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
											    .accept(MediaType.APPLICATION_JSON)
											    .type(MediaType.APPLICATION_JSON)
											    .put(new GenericType<ServiceResponse<Long>>() { }, se);
		
		// now try to get the scheduled experiment
		ServiceResponse<List<ScheduledExperiment>> sr_list = resource().path(ServiceConfiguration.SVC_EXECUTE)
														        	   .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
														        	   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
														        	   .accept(MediaType.APPLICATION_JSON)
														        	   .get(new GenericType<ServiceResponse<List<ScheduledExperiment>>>() { });

		// check the list for my added scenario to execution list
		for (int i = 0; i < sr_list.getObject().size(); i++) {
			
			ScheduledExperiment scheduledExperiment = sr_list.getObject().get(i);
			
			// if the scenario in the list is the shortly added one, we have to check it's attributes
			if (scheduledExperiment.getId() == sr_id.getObject()) {
				
				assertEquals(repeating, scheduledExperiment.isRepeating()); // harcoded request @TODO
				assertEquals(controllerURL, scheduledExperiment.getControllerUrl());
				assertEquals(label, scheduledExperiment.getLabel());
				assertEquals(accountId, scheduledExperiment.getAccountId());
				assertEquals(active, scheduledExperiment.isActive());
				
			}
			
		}
		*/
	}

	/**
	 * Test the enabling disabling feature of the schedulede experiment.
	 * 
	 * 1. login
	 * 2. get account related to user
	 * 3. add scenario
	 * 4. switch scenario
	 * 5. get current scenario definition
	 * 6. add scheduled experiment
	 * 7. get ID of scheduled experiment
	 * 8. check if current scheduled experiment is enabled
	 * 9. disable scheduled experiment
	 * 10. check if scheduled experiment is disabled
	 * 11. enable scheduled experiment
	 * 12. check if scheduled experiment is enabled
	 * 13. delete scheduled experiment
	 * 14. check if scheduled experiment is deleted
	 */
	@Test
	public void testScheduleExperimentEnablingDisabling() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		/*
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();

		// account is needed for account id
		ServiceResponse<Account> sr_account = resource().path(ServiceConfiguration.SVC_ACCOUNT)
													    .path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
													    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
													    .accept(MediaType.APPLICATION_JSON)
													    .get(new GenericType<ServiceResponse<Account>>() { });
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });

		ServiceResponse<ScenarioDefinition> sr_sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
														      .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
														      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
														      .type(MediaType.APPLICATION_JSON)
														      .get(new GenericType<ServiceResponse<ScenarioDefinition>>() { });

		assertEquals(true, sr_sd.getObject() != null); // the user must have a scenario now
		
		boolean repeating 		= false;
		String controllerURL 	= "myCustomURL";
		String label 			= "myScheduledExperiment";
		long accountId 			= sr_account.getObject().getId();
		boolean scenarioActive 	= false; // must be false, otherwise will fail on scheduledExperiment integrity check when adding
		long addedTime 			= System.currentTimeMillis();
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sr_sd.getObject());
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		se.setActive(scenarioActive);
		se.setAddedTime(addedTime);
		
		// add to execution list
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_EXECUTE)
												  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
												  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
												  .accept(MediaType.APPLICATION_JSON)
												  .type(MediaType.APPLICATION_JSON)
												  .post(new GenericType<ServiceResponse<Boolean>>() { }, se);

		assertEquals(true, sr_b.getObject());
		
		// get if for the added scenario
		ServiceResponse<Long> sr_id = resource().path(ServiceConfiguration.SVC_EXECUTE)
										       .path(ServiceConfiguration.SVC_EXECUTE_ID)
										       .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
										       .accept(MediaType.APPLICATION_JSON)
										       .type(MediaType.APPLICATION_JSON)
										       .put(new GenericType<ServiceResponse<Long>>() { }, se);
		
		String id = String.valueOf(sr_id.getObject());
		
		ServiceResponse<ScheduledExperiment> sr_se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
															     .path(id)
															     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
															     .accept(MediaType.APPLICATION_JSON)
															     .type(MediaType.APPLICATION_JSON)
															     .get(new GenericType<ServiceResponse<ScheduledExperiment>>() { });
		
		assertEquals(scenarioActive, sr_se2.getObject().isActive());
		
		// now disable the scheduled experiment
		scenarioActive = false;
		
		sr_b = resource().path(ServiceConfiguration.SVC_EXECUTE)
				         .path(id)
				         .path(ServiceConfiguration.SVC_EXECUTE_DISABLE)
				         .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				         .accept(MediaType.APPLICATION_JSON)
				         .type(MediaType.APPLICATION_JSON)
				         .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		assertEquals(true, sr_b.getObject());
		
		sr_se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
					        .path(id)
					        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					        .accept(MediaType.APPLICATION_JSON)
					        .type(MediaType.APPLICATION_JSON)
					        .get(new GenericType<ServiceResponse<ScheduledExperiment>>() { });

		assertEquals(false, sr_se2.getObject().isActive());
		
		// now enable the scheulded experiment again
		scenarioActive = true;
		
		sr_b = resource().path(ServiceConfiguration.SVC_EXECUTE)
				         .path(id)
				         .path(ServiceConfiguration.SVC_EXECUTE_ENABLE)
				         .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				         .accept(MediaType.APPLICATION_JSON)
				         .type(MediaType.APPLICATION_JSON)
				         .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		assertEquals(true, sr_b.getObject());
		
		sr_se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
					        .path(id)
					        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					        .accept(MediaType.APPLICATION_JSON)
					        .type(MediaType.APPLICATION_JSON)
					        .get(new GenericType<ServiceResponse<ScheduledExperiment>>() { });

		// be aware! as no ExperimentSeries have been selected, the scenario is not turned active!
		assertEquals(false, sr_se2.getObject().isActive());
		
		// now delete the scheduled experiment
		sr_b = resource().path(ServiceConfiguration.SVC_EXECUTE)
				         .path(id)
				         .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				         .accept(MediaType.APPLICATION_JSON)
				         .type(MediaType.APPLICATION_JSON)
				         .delete(new GenericType<ServiceResponse<Boolean>>() { });
		
		assertEquals(true, sr_b.getObject());
		
		
		sr_se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
					        .path(id)
					        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					        .accept(MediaType.APPLICATION_JSON)
					        .type(MediaType.APPLICATION_JSON)
					        .get(new GenericType<ServiceResponse<ScheduledExperiment>>() { });
		
		// as the scenario does not exists with the given ID, a conflict is thrown!
		assertEquals(Status.CONFLICT, sr_se2.getStatus());*/
	}
	
	/**
	 * Requests ExecutionDetails of a not-run scheduled experiment. Therefor the execution
	 * details must be empty.
	 * 
	 * 1. login
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. get scenario definition
	 * 5. get ExecutedExperimentDetails of the current user
	 */
	@Test
	public void testGetExecutionDetails() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		/*
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });

		ServiceResponse<ScenarioDefinition> sr_sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
															  .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
															  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
															  .type(MediaType.APPLICATION_JSON)
															  .get(new GenericType<ServiceResponse<ScenarioDefinition>>() { });

		assertEquals(true, sr_sd.getObject() != null); // the user must have a scenario now
		
		ServiceResponse<List<ExecutedExperimentDetails>> sr_eed = resource().path(ServiceConfiguration.SVC_EXECUTE)
																	     	.path(ServiceConfiguration.SVC_EXECUTE_DETAILS)
																	        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
																	        .accept(MediaType.APPLICATION_JSON)
																	        .type(MediaType.APPLICATION_JSON)
																	        .get(new GenericType<ServiceResponse<List<ExecutedExperimentDetails>>>() { });

		assertEquals(true, sr_eed.getObject().isEmpty());*/
	}
	
}
