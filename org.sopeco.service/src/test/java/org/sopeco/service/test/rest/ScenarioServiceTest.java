package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.model.ScenarioDefinitionReader;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.shared.Message;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * The <code>ScenarioServiceTest</code> tests various features of the
 * <code>ScenarioService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class ScenarioServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioServiceTest.class);

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public ScenarioServiceTest() {
		super();
	}
	
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(TestConfiguration.PACKAGE_NAME_REST)
				//.contextListenerClass(StartUpService.class)
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
	 */
	@After
	public void cleanUpDatabase() {
		LOGGER.debug("Cleaning up the database.");
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String scenarioNameEmpty = TestConfiguration.TEST_CLEAN_SCENARIO_NAME;
		String measSpecNameEmpty = TestConfiguration.TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();

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
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now add scenario WITH THE SAME NAME a second time
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
							  .path(TEST_SCENARIO_NAME)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
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
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
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
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		// now try to delete the scenario
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
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
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now try to switch the scenario
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
							  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(Boolean.class);
		
		// the switch should be succesful
		assertEquals(b, true);
	}
	
	/**
	 * Try to extract a scenario out of the Service as xml file.
	 * The xml file is checked afterwards for correctness. When
	 * this test fails, then the scenario xml parsing might be
	 * corruput.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. get scenario as xml
	 * 5. get scenario as ScenarioDefinition
	 * 6. compare XML and ScenarioDefinition
	 */
	@Test
	public void testScenarioXMLParsing() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .type(MediaType.APPLICATION_JSON)
				  .delete(Boolean.class);
		
		// add a default scenario
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
		
		// switch to the newly created measurmentspecification
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
				  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
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
