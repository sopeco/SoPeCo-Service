package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.StartUpService;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.shared.Message;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * The {@link MeasurementEnvironmentDefinitionServiceTest} tests the {@link MeasurementEnvironmentDefinitionService} class.
 * E.g. MED adding, parameter namespace updating...
 * 
 * @author Peter Merkert
 */
public class MeasurementEnvironmentDefinitionServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementEnvironmentDefinitionServiceTest.class.getName());
	
	private static final String SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public MeasurementEnvironmentDefinitionServiceTest() {
		super();
	}

	/**
	 * Configure is called on the object creation of a JerseyTest. It's used to
	 * configure where the JerseyTest can find JSON, the REST service to test
	 * and the JSON POJO.
	 * 
	 * @return the configuration
	 */
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(TestConfiguration.PACKAGE_NAME_REST)
				.contextListenerClass(StartUpService.class)
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
	    // the class contains the configuration to ignore not mappable properties
	    config.getClasses().add(CustomObjectWrapper.class);
	    return config;
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
		
		// delete the example scenario
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
			      .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
			      .delete(Boolean.class);
	}
	
	/**
	 * Sets the MeasurementEnvironmentDefinition of a scenario to a blank new one.
	 * 
	 * 1. log in
	 * 2. set blank MED
	 */
	@Test
	public void testBlankMED() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// blank the MeasurementEnvironmentDefinition
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MED)
														 .path(ServiceConfiguration.SVC_MED_SET)
														 .path(ServiceConfiguration.SVC_MED_SET_BLANK)
													     .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
													     .put(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
	}
	
	/**
	 * Tests the status of a MED with checking the root parameter namespace name. It must
	 * be "root" otherwise something went wrong.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. get current MED
	 */
	@Test
	public void testCurrentMED() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// return the MED for the current user
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MED)
														 .path(ServiceConfiguration.SVC_MED_CURRENT)
													     .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
													     .get(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
	}
	
	/**
	 * Test MED namespace adding. A custom namespace is added to the current MED
	 * and checked afterwards.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. add namespace
	 * 5. get current MED
	 */
	@Test
	public void testMEDNamespaceAdding() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String mynamespace = "mynamespacepath";
		String mynamespaceFullPath = "root/" + mynamespace;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// return the MED for the current user
		Boolean b = resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
							  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .put(Boolean.class);
		
		assertEquals(true, b);
		
		// return the MED for the current user
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MED)
														 .path(ServiceConfiguration.SVC_MED_CURRENT)
													     .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
													     .get(MeasurementEnvironmentDefinition.class);
				
		
		// as the namespace is not set yet, it must be null
		assertEquals(mynamespace, med.getRoot().getChildren().get(0).getName());
		assertEquals("root" + "." + mynamespace, med.getRoot().getChildren().get(0).getFullName());
	}
	
	/**
	 * Tests the removing of a parameter namespace.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. add parameter namespace
	 * 5. remove parameter namespace
	 */
	@Test
	public void testMEDNamespaceRemoving() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String mynamespace = "mynamespacepath";
		String mynamespaceFullPath = "root/" + mynamespace;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		// return the MED for the current user
		Boolean b = resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
							  .path(ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .delete(Boolean.class);
		
		// removal must succeed
		assertEquals(true, b);
		
	}
	
	/**
	 * Tests the renaming service.
	 * 
	 * 1. login
	 * 2. add scenario
	 * 3. switch to newly created scenario
	 * 4. add new namespace
	 * 5. rename namespace
	 * 6. check current namespace name
	 * 7. check invalid token failure
	 * 8. remove namespace
	 * 9. cehck to fail at renaming the removed namespace
	 */
	@Test
	public void testMEDNamespaceRenaming() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String mynamespace = "mynamespacepath";
		String mynamespaceFullPath = "root/" + mynamespace;
		String mynamespaceNewName = "mynamespacepathnew";
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		Boolean b = resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
							  .path(ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW, mynamespaceNewName)
						      .put(Boolean.class);
		
		// the renaming must succeed
		assertEquals(true, b);
		
		// return the MED for the current user
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MED)
														 .path(ServiceConfiguration.SVC_MED_CURRENT)
													     .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
													     .get(MeasurementEnvironmentDefinition.class);
				
		// as the namespace is not set yet, it must be null
		assertEquals(mynamespaceNewName, med.getRoot().getChildren().get(0).getName());
		assertEquals("root" + "." + mynamespaceNewName, med.getRoot().getChildren().get(0).getFullName());
		
		
		// test not valid token
		b = resource().path(ServiceConfiguration.SVC_MED)
					  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
					  .path(ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
				      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, "123")
				      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW, mynamespaceNewName)
				      .put(Boolean.class);
		
		assertEquals(false, b);
		
		// test not available namespace (delete once for safety)
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .delete(Boolean.class);
		
		b = resource().path(ServiceConfiguration.SVC_MED)
					  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
					  .path(ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
				      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW, mynamespaceNewName)
				      .put(Boolean.class);
		
		assertEquals(false, b);
	}
	
	/**
	 * Tests adding a custom parameter to a custom parameter namespace.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. add parameter namespace
	 * 5. add parameter
	 */
	@Test
	public void testMEDParameterAdding() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String mynamespace = "mynamespacepath";
		String mynamespaceFullPath = "root/" + mynamespace;
		String paramName = "myparam";
		String paramType = "myparamtype"; // be aware, after setting this is uppercase
		ParameterRole paramRole = ParameterRole.INPUT;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		Boolean b = resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_PARAM)
							  .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
							  .type(MediaType.APPLICATION_JSON)
						      .put(Boolean.class, paramRole);

		assertEquals(true, b);
	}
	
	/**
	 * Tests updating a already added parameter for a given scenario.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. add parameter namespace
	 * 5. add a parameter
	 * 6. update the parameter
	 */
	@Test
	public void testMEDParameterUpdating() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String mynamespace = "mynamespacepath";
		String mynamespaceFullPath = "root/" + mynamespace;
		String paramName = "myparam";
		String paramNameNew = "mynewparam";
		String paramType = "myparamtype"; // be aware, after setting this is uppercase
		ParameterRole paramRole = ParameterRole.INPUT;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_PARAM)
				  .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
			      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
				  .type(MediaType.APPLICATION_JSON)
			      .put(Boolean.class, paramRole);

		Boolean b = resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_PARAM)
							  .path(ServiceConfiguration.SVC_MED_PARAM_UPDATE)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME_NEW, paramNameNew)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
							  .type(MediaType.APPLICATION_JSON)
						      .put(Boolean.class, paramRole);
		
		assertEquals(true, b);
	}

	/**
	 * Tests the removal of a set parameter in a custom parameter namespace.
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. add parameter namespace
	 * 5. add parameter
	 * 6. remove parameter
	 */
	@Test
	public void testMEDParameterRemoving() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String mynamespace = "mynamespacepath";
		String mynamespaceFullPath = "root/" + mynamespace;
		String paramName = "myparam";
		String paramType = "myparamtype"; // be aware, after setting this is uppercase
		ParameterRole paramRole = ParameterRole.INPUT;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_PARAM)
							  .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
							  .type(MediaType.APPLICATION_JSON)
						      .put(Boolean.class, paramRole);

		Boolean b = resource().path(ServiceConfiguration.SVC_MED)
							  .path(ServiceConfiguration.SVC_MED_PARAM)
							  .path(ServiceConfiguration.SVC_MED_PARAM_REMOVE)
						      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
						      .delete(Boolean.class);
		
		// deletion must have been succesful
		assertEquals(true, b);
	}
}
