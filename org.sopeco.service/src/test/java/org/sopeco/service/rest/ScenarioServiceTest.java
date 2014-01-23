package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.sopeco.engine.model.ScenarioDefinitionReader;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.shared.Message;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class ScenarioServiceTest extends JerseyTest {

	public ScenarioServiceTest() {
		super();
	}
	
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(TestConfiguration.PACKAGE_NAME_REST)
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
	 * Try adding two scenarios with the same name. The second addition must fail.
	 * 
	 * 1. log in
	 * 2. add new scenario with name X
	 * 3. add again new scenario with name X
	 */
	@Test
	public void testScenarioDoubleAdd() {
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
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we check the double adding afterwards)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now add scenario WITH THE SAME NAME a second time
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
							  .path(scenarioname)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .type(MediaType.APPLICATION_JSON)
							  .post(Boolean.class, esd);
		
		assertEquals(b, false);
	}

	/**
	 * Test the scenario listing. Checks the list for a newly created scenario.
	 * 
	 * 1. log in
	 * 2. add new scenario
	 * 3. list all scenarios
	 */
	@Test
	public void testScenarioListing() {
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
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we don't care about this issue here)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now check if at least one scenario is in the list
		String[] list = resource().path(ServiceConfiguration.SVC_SCENARIO)
							   .path(ServiceConfiguration.SVC_SCENARIO_LIST)
							   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							   .accept(MediaType.APPLICATION_JSON)
							   .get(String[].class);
		
		assertEquals(list.length > 0, true);
	}

	/**
	 * Try to delete a newly created scenario.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. delete scenario
	 */
	@Test
	public void testScenarioDeletion() {
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
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we don't care here about it)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now try to delete the scenario
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .delete(Boolean.class);
		
		// the deletion should be successful completed
		assertEquals(true, b);
	}

	/**
	 * Try to switch to a newly created scenario
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 */
	@Test
	public void testScenarioSwitch() {
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
		
		// add a default scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now try to switch the scenario
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
							  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(Boolean.class);
		
		// the switch should be succesful
		assertEquals(b, true);
	}
	
	@Test
	public void testScenarioLoad() {
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
		
		// add a default scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now try to switch the scenario
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
							  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(Boolean.class);
		
		// the switch should be succesful
		assertEquals(b, true);
	}
	
	@Test
	public void testScenarioXMLParsing() {
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
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .type(MediaType.APPLICATION_JSON)
				  .delete(Boolean.class);
		
		// add a default scenario
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
		
		// switch to the newly created measurmentspecification
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
				  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurmentspecificationname)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		String xml = resource().path(ServiceConfiguration.SVC_SCENARIO)
							   .path(ServiceConfiguration.SVC_SCENARIO_XML)
							   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							   .accept(MediaType.APPLICATION_JSON)
							   .get(String.class);
		
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MED)
														 .path(ServiceConfiguration.SVC_MED_CURRENT)
														 .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
														 .accept(MediaType.APPLICATION_JSON)
														 .get(MeasurementEnvironmentDefinition.class);
		
		ScenarioDefinitionReader sdr = new ScenarioDefinitionReader(med, token);
		ScenarioDefinition scenarioDefinitionXML = sdr.readFromString(xml);
		
		ScenarioDefinition scenarioDefinitionCurrent = resource().path(ServiceConfiguration.SVC_SCENARIO)
															     .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
															     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
															     .accept(MediaType.APPLICATION_JSON)
															     .get(ScenarioDefinition.class);
		
		assertEquals(true, scenarioDefinitionCurrent.equals(scenarioDefinitionXML));
		
	}
}
