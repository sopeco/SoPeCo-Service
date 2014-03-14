package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.ExecutionService;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The {@link ExecutionServiceTest} tests various features of the
 * {@link ExecutionService} RESTful services.
 * 
 * @author Peter Merkert
 */
public class ExecutionServiceTest extends AbstractServiceTest {

	/**
	 * The TEST_SCENARIO_NAME is used to shorten the access to the test variable for the scenario name.
	 */
	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	
	/**
	 * The TEST_MEASUREMENT_SPECIFICATION_NAME is used to shorten the access to the test variable meas. spec. name.
	 */
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
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
		
		String token = login(accountname, password);
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
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

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
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
		
		logout(token);
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
		
		String token = login(accountname, password);

		// account is needed for account id
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
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
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
				    .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
				    .post(Entity.entity(se, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		long id = r.readEntity(Long.class);
		
		// now try to get the scheduled experiment
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
	        	    .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	        	    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
	        	    .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		List<ScheduledExperiment> SElist = r.readEntity(new GenericType<List<ScheduledExperiment>>() { });
		
		// check the list for my added scenario to execution list
		for (int i = 0; i < SElist.size(); i++) {
			
			ScheduledExperiment scheduledExperiment = SElist.get(i);
			
			// if the scenario in the list is the shortly added one, we have to check it's attributes
			if (scheduledExperiment.getId() == id) {
				
				assertEquals(repeating, scheduledExperiment.isRepeating()); // harcoded request @TODO
				assertEquals(controllerURL, scheduledExperiment.getControllerUrl());
				assertEquals(label, scheduledExperiment.getLabel());
				assertEquals(accountId, scheduledExperiment.getAccountId());
				assertEquals(active, scheduledExperiment.isActive());
				
			}
			
		}
		
		logout(token);
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
		
		String token = login(accountname, password);

		// account is needed for account id
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
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
		boolean scenarioActive 	= false; // must be false, otherwise will fail on scheduledExperiment integrity check when adding
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId);
		se.setControllerUrl(controllerURL);
		se.setRepeating(repeating);
		se.setLabel(label);
		se.setActive(scenarioActive);
		
		// add to execution list
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
				    .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
				    .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
					.request(MediaType.APPLICATION_JSON)
				    .post(Entity.entity(se, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		long id = r.readEntity(Long.class);
		
		r =  target().path(ServiceConfiguration.SVC_EXECUTE)
			         .path(String.valueOf(id))
			         .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			         .request(MediaType.APPLICATION_JSON)
			         .get();
		
		ScheduledExperiment se2 = r.readEntity(ScheduledExperiment.class);
		
		assertEquals(scenarioActive, se2.isActive());
		
		// now disable the scheduled experiment
		scenarioActive = false;
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
		         	.path(String.valueOf(id))
		            .path(ServiceConfiguration.SVC_EXECUTE_DISABLE)
		            .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			        .request(MediaType.APPLICATION_JSON)
					.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		r =  target().path(ServiceConfiguration.SVC_EXECUTE)
			         .path(String.valueOf(id))
			         .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			         .request(MediaType.APPLICATION_JSON)
			         .get();
		
		se2 = r.readEntity(ScheduledExperiment.class);
		
		assertEquals(scenarioActive, se2.isActive());
		
		// now enable the scheulded experiment again
		scenarioActive = true;
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
		         	.path(String.valueOf(id))
		            .path(ServiceConfiguration.SVC_EXECUTE_ENABLE)
		            .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			        .request(MediaType.APPLICATION_JSON)
					.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		assertEquals(Status.CONFLICT.getStatusCode(), r.getStatus());
		
		r =  target().path(ServiceConfiguration.SVC_EXECUTE)
		         .path(String.valueOf(id))
		         .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
		         .request(MediaType.APPLICATION_JSON)
		         .get();
	
		se2 = r.readEntity(ScheduledExperiment.class);

		// be aware! as no ExperimentSeries have been selected, the scenario is not turned active!
		assertEquals(false, se2.isActive());
		
		// now delete the scheduled experiment
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
	         		.path(String.valueOf(id))
		            .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			        .request(MediaType.APPLICATION_JSON)
		            .delete();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
         		    .path(String.valueOf(id))
			        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			        .request(MediaType.APPLICATION_JSON)
			        .get();
		
		// as the scenario does not exists with the given ID, a conflict is thrown!
		assertEquals(Status.CONFLICT.getStatusCode(), r.getStatus());
		
		logout(token);
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
		
		String token = login(accountname, password);
		
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

		Response r = target().path(ServiceConfiguration.SVC_SCENARIO)
				    		 .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
				    		 .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    		 .request(MediaType.APPLICATION_JSON)
				    		 .get();

		ScenarioDefinition sd = r.readEntity(ScenarioDefinition.class);
		
		assertEquals(true, sd != null); // the user must have a scenario now
		
		r = target().path(ServiceConfiguration.SVC_EXECUTE)
			     	.path(ServiceConfiguration.SVC_EXECUTE_DETAILS)
				    .queryParam(ServiceConfiguration.SVCP_EXECUTE_SCENARIONAME, TEST_SCENARIO_NAME)
			        .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
			        .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		List<ExecutedExperimentDetails> eed = r.readEntity(new GenericType<List<ExecutedExperimentDetails>>() { });
		
		// TODO: delete ExecutedExperimentDetails in database first on cleanup
		//assertEquals(true, eed.isEmpty());
		
		logout(token);
	}
	
}
