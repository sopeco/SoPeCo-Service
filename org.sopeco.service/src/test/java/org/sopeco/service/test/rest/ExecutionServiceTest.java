package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.shared.Message;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * The <code>ExecutionServiceTest</code> tests various features of the
 * <code>ExecutionService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class ExecutionServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionServiceTest.class);
	
	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public ExecutionServiceTest() {
		super();
	}
	
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(TestConfiguration.PACKAGE_NAME_REST)
				.initParam(TestConfiguration.PACKAGE_NAME_POJO, "true")
				.clientConfig(createClientConfig())
				.build();
	}

	/**
	 * Sets the client config for the client. The method adds a special {@link CustomObjectWrapper}
	 * to the normal Jackson wrapper for JSON.
	 * This method is called by {@link configure()}.
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new DefaultClientConfig();
	    config.getClasses().add(CustomObjectWrapper.class);
	    return config;
	}

	/**
	 * Cleans up the database means: Delete the scenario with name {@link 
	 * MeasurementControllerServiceTest.SCENARIO_NAME} in the database. This scenario
	 * is used by every single test and it can cause errors, when the scenario is created,
	 * but already in the database. Because the database instance is then not updated,
	 * which can result in unexpected behaviour.
	 * <br />
	 * In addition all the schedulede scenarios for the test account are deleted.
	 */
	@After
	public void cleanUpDatabase() {
		LOGGER.debug("Cleaning up the database.");
		
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String scenarioNameEmpty = TestConfiguration.TEST_CLEAN_SCENARIO_NAME;
		String measSpecNameEmpty = TestConfiguration.TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();

		// clean the scheduling list for the user
		resource().path(ServiceConfiguration.SVC_EXECUTE)
				  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .type(MediaType.APPLICATION_JSON)
				  .delete(Boolean.class);

		// now create empty scenario to delete the test scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioNameEmpty)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measSpecNameEmpty)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioNameEmpty)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// delete the example scneario from the db
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
			      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
			      .delete(Boolean.class);
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
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		Account account = resource().path(ServiceConfiguration.SVC_ACCOUNT)
								    .path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
								    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
								    .accept(MediaType.APPLICATION_JSON)
								    .get(Account.class);
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);

		ScenarioDefinition sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
										  .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
										  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
										  .type(MediaType.APPLICATION_JSON)
										  .get(ScenarioDefinition.class);

		assertEquals(true, sd != null); // the user must have a scenario now
		
		boolean repeating = false;
		String controllerURL = "myCustomURL";
		String label = "myScheduledExperiment";
		long accountId = account.getId();
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		
		Boolean b = resource().path(ServiceConfiguration.SVC_EXECUTE)
							  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .type(MediaType.APPLICATION_JSON)
							  .post(Boolean.class, se);

		assertEquals(true, b);
		
		b = resource().path(ServiceConfiguration.SVC_EXECUTE)
					  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
					  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					  .type(MediaType.APPLICATION_JSON)
					  .delete(Boolean.class);
		
		assertEquals(true, b);
		
		// now try to get the scheduled experiment (which must be empty)
		List<ScheduledExperiment> list = (List<ScheduledExperiment>) resource().path(ServiceConfiguration.SVC_EXECUTE)
																		       .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
																		       .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
																		       .accept(MediaType.APPLICATION_JSON)
																		       .get(new GenericType<List<ScheduledExperiment>>() { });
		
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
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();

		// account is needed for account id
		Account account = resource().path(ServiceConfiguration.SVC_ACCOUNT)
								    .path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
								    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
								    .accept(MediaType.APPLICATION_JSON)
								    .get(Account.class);
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);

		ScenarioDefinition sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
										  .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
										  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
										  .type(MediaType.APPLICATION_JSON)
										  .get(ScenarioDefinition.class);

		assertEquals(true, sd != null); // the user must have a scenario now
		
		boolean repeating = false;
		String controllerURL = "myCustomURL";
		String label = "myScheduledExperiment";
		long accountId = account.getId();
		boolean active = false;
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		se.setActive(active);
		
		Boolean b = resource().path(ServiceConfiguration.SVC_EXECUTE)
							  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .type(MediaType.APPLICATION_JSON)
							  .post(Boolean.class, se);

		assertEquals(true, b);

		// get the id of the schedulede experiment
		long id = resource().path(ServiceConfiguration.SVC_EXECUTE)
						    .path(ServiceConfiguration.SVC_EXECUTE_ID)
						    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
						    .accept(MediaType.APPLICATION_JSON)
						    .type(MediaType.APPLICATION_JSON)
						    .put(long.class, se);
		
		// now try to get the scheduled experiment
		List<ScheduledExperiment> list = (List<ScheduledExperiment>) resource().path(ServiceConfiguration.SVC_EXECUTE)
																		       .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
																		       .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
																		       .accept(MediaType.APPLICATION_JSON)
																		       .get(new GenericType<List<ScheduledExperiment>>() { });
		
		// check the list for my added scenario to execution list
		for (int i = 0; i < list.size(); i++) {
			
			ScheduledExperiment scheduledExperiment = list.get(i);
			
			// if the scenario in the list is the shortly added one, we have to check it's attributes
			if (scheduledExperiment.getId() == id) {
				
				assertEquals(repeating, scheduledExperiment.isRepeating()); // harcoded request @TODO
				assertEquals(controllerURL, scheduledExperiment.getControllerUrl());
				assertEquals(label, scheduledExperiment.getLabel());
				assertEquals(accountId, scheduledExperiment.getAccountId());
				assertEquals(active, scheduledExperiment.isActive());
				
			}
			
		}
		
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
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();

		// account is needed for account id
		Account account = resource().path(ServiceConfiguration.SVC_ACCOUNT)
								    .path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
								    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, token)
								    .accept(MediaType.APPLICATION_JSON)
								    .get(Account.class);
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);

		ScenarioDefinition sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
										  .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
										  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
										  .type(MediaType.APPLICATION_JSON)
										  .get(ScenarioDefinition.class);

		assertEquals(true, sd != null); // the user must have a scenario now
		
		boolean repeating = false;
		String controllerURL = "myCustomURL";
		String label = "myScheduledExperiment";
		long accountId = account.getId();
		boolean scenarioActive = true;
		long addedTime = System.currentTimeMillis();
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		se.setActive(scenarioActive);
		se.setAddedTime(addedTime);
		
		// add to execution list
		Boolean b = resource().path(ServiceConfiguration.SVC_EXECUTE)
							  .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .type(MediaType.APPLICATION_JSON)
							  .post(Boolean.class, se);

		assertEquals(true, b);
		
		// get if for the added scenario
		long tmpID = resource().path(ServiceConfiguration.SVC_EXECUTE)
						    .path(ServiceConfiguration.SVC_EXECUTE_ID)
						    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
						    .accept(MediaType.APPLICATION_JSON)
						    .type(MediaType.APPLICATION_JSON)
						    .put(long.class, se);
		
		String id = String.valueOf(tmpID);
		
		ScheduledExperiment se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
										     .path(id)
										     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
										     .accept(MediaType.APPLICATION_JSON)
										     .type(MediaType.APPLICATION_JSON)
										     .get(ScheduledExperiment.class);
		
		assertEquals(scenarioActive, se2.isActive());
		
		// now disable the scheduled experiment
		scenarioActive = false;
		
		b = resource().path(ServiceConfiguration.SVC_EXECUTE)
				      .path(id)
				      .path(ServiceConfiguration.SVC_EXECUTE_DISABLE)
				      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				      .accept(MediaType.APPLICATION_JSON)
				      .type(MediaType.APPLICATION_JSON)
				      .put(Boolean.class);
		
		assertEquals(true, b);
		
		se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
					     .path(id)
					     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					     .accept(MediaType.APPLICATION_JSON)
					     .type(MediaType.APPLICATION_JSON)
					     .get(ScheduledExperiment.class);

		assertEquals(scenarioActive, se2.isActive());
		
		// now enable the scheulded experiment again
		scenarioActive = true;
		
		b = resource().path(ServiceConfiguration.SVC_EXECUTE)
				      .path(id)
				      .path(ServiceConfiguration.SVC_EXECUTE_ENABLE)
				      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				      .accept(MediaType.APPLICATION_JSON)
				      .type(MediaType.APPLICATION_JSON)
				      .put(Boolean.class);
		
		assertEquals(true, b);
		
		se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
					     .path(id)
					     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					     .accept(MediaType.APPLICATION_JSON)
					     .type(MediaType.APPLICATION_JSON)
					     .get(ScheduledExperiment.class);

		assertEquals(scenarioActive, se2.isActive());
		
		// now delete the scheduled experiment
		b = resource().path(ServiceConfiguration.SVC_EXECUTE)
				      .path(id)
				      .path(ServiceConfiguration.SVC_EXECUTE_DELETE)
				      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				      .accept(MediaType.APPLICATION_JSON)
				      .type(MediaType.APPLICATION_JSON)
				      .delete(Boolean.class);
		
		assertEquals(true, b);
		
		try {
			se2 =  resource().path(ServiceConfiguration.SVC_EXECUTE)
						     .path(id)
						     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
						     .accept(MediaType.APPLICATION_JSON)
						     .type(MediaType.APPLICATION_JSON)
						     .get(ScheduledExperiment.class);
		} catch (UniformInterfaceException ex) {

            final int status = ex.getResponse().getStatus();
            assertEquals(HttpStatus.NO_CONTENT_204.getStatusCode(), status);
            
        }
		
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
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// add scenario and switch to
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);

		ScenarioDefinition sd = resource().path(ServiceConfiguration.SVC_SCENARIO)
										  .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
										  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
										  .type(MediaType.APPLICATION_JSON)
										  .get(ScenarioDefinition.class);

		assertEquals(true, sd != null); // the user must have a scenario now
		
		List<ExecutedExperimentDetails> eed = resource().path(ServiceConfiguration.SVC_EXECUTE)
													    .path(ServiceConfiguration.SVC_EXECUTE_DETAILS)
													    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
													    .accept(MediaType.APPLICATION_JSON)
													    .type(MediaType.APPLICATION_JSON)
													    .get(new GenericType<List<ExecutedExperimentDetails>>() { });

		assertEquals(true, eed.isEmpty());
	}
	
}
