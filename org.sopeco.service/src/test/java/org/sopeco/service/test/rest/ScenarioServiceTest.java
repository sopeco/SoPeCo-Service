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
import org.sopeco.service.rest.exchange.ServiceResponse;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.GenericType;
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
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();

		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioNameEmpty)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measSpecNameEmpty)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioNameEmpty)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// delete the example scenario
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
			      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TestConfiguration.TEST_SCENARIO_NAME)
			      .delete(new GenericType<ServiceResponse<Boolean>>() { });
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
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();
		
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
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		// now add scenario WITH THE SAME NAME a second time
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_SCENARIO)
												  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
												  .path(TEST_SCENARIO_NAME)
												  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
												  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
												  .accept(MediaType.APPLICATION_JSON)
												  .type(MediaType.APPLICATION_JSON)
												  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		assertEquals(false, sr_b.getObject());
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
		
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();
		
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
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		// now check if at least one scenario is in the list
		ServiceResponse<String[]> sr_list = resource().path(ServiceConfiguration.SVC_SCENARIO)
													  .path(ServiceConfiguration.SVC_SCENARIO_LIST)
													  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
													  .accept(MediaType.APPLICATION_JSON)
													  .get(new GenericType<ServiceResponse<String[]>>() { });
		
		assertEquals(true, sr_list.getObject().length > 0);
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

		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();
		
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
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);

		// now try to delete the scenario
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .delete(new GenericType<ServiceResponse<Boolean>>() { });
		
		// the deletion should be successful completed
		assertEquals(true, sr_b.getObject());
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
		
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();
		
		// add a default scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		// now try to switch the scenario
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_SCENARIO)
												  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
												  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
												  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
												  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
												  .accept(MediaType.APPLICATION_JSON)
												  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// the switch should be succesful
		assertEquals(true, sr_b.getObject());
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

		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });
		
		String token = sr.getObject();
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .type(MediaType.APPLICATION_JSON)
				  .delete(new GenericType<ServiceResponse<Boolean>>() { });
		
		// add a default scenario
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
		
		// switch to the newly created measurmentspecification
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
				  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		ServiceResponse<String> sr_xml = resource().path(ServiceConfiguration.SVC_SCENARIO)
												   .path(ServiceConfiguration.SVC_SCENARIO_XML)
												   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
												   .accept(MediaType.APPLICATION_JSON)
												   .get(new GenericType<ServiceResponse<String>>() { });
		
		ServiceResponse<MeasurementEnvironmentDefinition> sr_med = resource().path(ServiceConfiguration.SVC_MED)
																			 .path(ServiceConfiguration.SVC_MED_CURRENT)
																			 .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
																			 .accept(MediaType.APPLICATION_JSON)
																			 .get(new GenericType<ServiceResponse<MeasurementEnvironmentDefinition>>() { });
		
		ScenarioDefinitionReader sdr = new ScenarioDefinitionReader(sr_med.getObject(), token);
		ScenarioDefinition scenarioDefinitionXML = sdr.readFromString(sr_xml.getObject());
		
		ServiceResponse<ScenarioDefinition> sr_scenarioDefinitionCurrent = resource().path(ServiceConfiguration.SVC_SCENARIO)
																				     .path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
																				     .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
																				     .accept(MediaType.APPLICATION_JSON)
																				     .get(new GenericType<ServiceResponse<ScenarioDefinition>>() { });
		
		assertEquals(true, sr_scenarioDefinitionCurrent.getObject().equals(scenarioDefinitionXML));
	}
}
