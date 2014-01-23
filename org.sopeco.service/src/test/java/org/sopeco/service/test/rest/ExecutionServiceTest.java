package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.shared.Message;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class ExecutionServiceTest extends JerseyTest {

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

	@Test
	public void testScheduleExperiment() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String measurmentspecificationname = "examplespecname";
		String scenarioname = "examplescenario";
		
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
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
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
		int accountId = 1;
		
		ScheduledExperiment se = new ScheduledExperiment();
		se.setScenarioDefinition(sd);
		se.setAccountId(accountId); // harcoded one can cause errors
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
		
		// now try to get the scheduled experiment
		List<ScheduledExperiment> list = (List<ScheduledExperiment>)resource().path(ServiceConfiguration.SVC_EXECUTE)
																		      .path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
																		      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
																		      .accept(MediaType.APPLICATION_JSON)
																		      .get(new GenericType<List<ScheduledExperiment>>(){});
		
		assertEquals(repeating, list.get(0).isRepeating());
		//assertEquals(controllerURL, list.get(0).getControllerUrl());
		assertEquals(label, list.get(0).getLabel());
		assertEquals(accountId, list.get(0).getAccountId());
	}

}
