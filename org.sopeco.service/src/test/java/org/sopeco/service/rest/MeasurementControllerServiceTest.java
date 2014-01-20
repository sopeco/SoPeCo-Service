package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.shared.MECStatus;
import org.sopeco.service.shared.Message;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class MeasurementControllerServiceTest extends JerseyTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementControllerServiceTest.class.getName());
	
	public MeasurementControllerServiceTest() {
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
		return new WebAppDescriptor.Builder(new String[] {
				TestConfiguration.PACKAGE_NAME_JSON,
				TestConfiguration.PACKAGE_NAME_REST })
				.initParam(TestConfiguration.PACKAGE_NAME_POJO, "true")
				.clientConfig(createClientConfig())
				.build();
	}

	/**
	 * Sets the client config for the client to accept JSON and
	 * converting JSON Object to POJOs.
	 * This method is called by {@link configure()}.
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new DefaultClientConfig();
	    // the class contains the configuration to ignore not mappable properties
	    config.getClasses().add(JacksonJaxbJsonProvider.class);
	    config.getClasses().add(CustomObjectWrapper.class);
	    config.getFeatures().put(TestConfiguration.PACKAGE_NAME_POJO, Boolean.TRUE);
	    return config;
	}
	
	@Test
	public void testMECStatus() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// connect to a random string
		MECStatus mecStatus= resource().path(ServiceConfiguration.SVC_MEC)
									   .path(ServiceConfiguration.SVC_MEC_CHECK)
									   .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
									   .queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
									   .get(MECStatus.class);
		
		assertEquals(MECStatus.NO_VALID_MEC_URL, mecStatus.getStatus());
	
		// check if a wrong token fails, too
		mecStatus= resource().path(ServiceConfiguration.SVC_MEC)
						     .path(ServiceConfiguration.SVC_MEC_CHECK)
						     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, "myrandomtoken")
						     .queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
						     .get(MECStatus.class);
		
		assertEquals(-1, mecStatus.getStatus());
		
	}
	
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// blank the MeasurementEnvironmentDefinition
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MEC)
														 .path(ServiceConfiguration.SVC_MEC_MED)
														 .path(ServiceConfiguration.SVC_MEC_MED_SET)
														 .path(ServiceConfiguration.SVC_MEC_MED_SET_BLANK)
													     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
													     .put(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
	}
	
	
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// return the MED for the current user
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MEC)
														 .path(ServiceConfiguration.SVC_MEC_CURRENT)
													     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
													     .get(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
	}
	
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// return the MED for the current user
		Boolean b = resource().path(ServiceConfiguration.SVC_MEC)
							  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
							  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_ADD)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
						      .put(Boolean.class);
		
		assertEquals(true, b);
		
		// return the MED for the current user
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MEC)
														 .path(ServiceConfiguration.SVC_MEC_CURRENT)
													     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
													     .get(MeasurementEnvironmentDefinition.class);
				
		
		// as the namespace is not set yet, it must be null
		assertEquals(mynamespace, med.getRoot().getChildren().get(0).getName());
		assertEquals("root" + "." + mynamespace, med.getRoot().getChildren().get(0).getFullName());
	}
	
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace
		resource().path(ServiceConfiguration.SVC_MEC)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		// return the MED for the current user
		Boolean b = resource().path(ServiceConfiguration.SVC_MEC)
							  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
							  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_REMOVE)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MEC)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		Boolean b = resource().path(ServiceConfiguration.SVC_MEC)
							  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
							  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_RENAME)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE_NEW, mynamespaceNewName)
						      .put(Boolean.class);
		
		// the renaming must succeed
		assertEquals(true, b);
		
		// return the MED for the current user
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MEC)
														 .path(ServiceConfiguration.SVC_MEC_CURRENT)
													     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
													     .get(MeasurementEnvironmentDefinition.class);
				
		// as the namespace is not set yet, it must be null
		assertEquals(mynamespaceNewName, med.getRoot().getChildren().get(0).getName());
		assertEquals("root" + "." + mynamespaceNewName, med.getRoot().getChildren().get(0).getFullName());
		
		
		// test not available token
		b = resource().path(ServiceConfiguration.SVC_MEC)
					  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
					  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_RENAME)
				      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, "123")
				      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
				      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE_NEW, mynamespaceNewName)
				      .put(Boolean.class);
		
		assertEquals(false, b);
		
		// test not available namespace (delete once for safety)
		resource().path(ServiceConfiguration.SVC_MEC)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_REMOVE)
			      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
			      .delete(Boolean.class);
		
		b = resource().path(ServiceConfiguration.SVC_MEC)
					  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
					  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_RENAME)
				      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
				      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
				      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE_NEW, mynamespaceNewName)
				      .put(Boolean.class);
		
		assertEquals(false, b);
	}
	
	
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MEC)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		Boolean b = resource().path(ServiceConfiguration.SVC_MEC)
							  .path(ServiceConfiguration.SVC_MEC_PARAM)
							  .path(ServiceConfiguration.SVC_MEC_PARAM_ADD)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_NAME, paramName)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_TYP, paramType)
							  .type(MediaType.APPLICATION_JSON)
						      .put(Boolean.class, paramRole);

		assertEquals(true, b);
	}
	
	
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
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// create the namespace, to ensure to have at least this one
		resource().path(ServiceConfiguration.SVC_MEC)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MEC_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
			      .put(Boolean.class);
		
		resource().path(ServiceConfiguration.SVC_MEC)
							  .path(ServiceConfiguration.SVC_MEC_PARAM)
							  .path(ServiceConfiguration.SVC_MEC_PARAM_ADD)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_NAME, paramName)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_TYP, paramType)
							  .type(MediaType.APPLICATION_JSON)
						      .put(Boolean.class, paramRole);

		Boolean b = resource().path(ServiceConfiguration.SVC_MEC)
							  .path(ServiceConfiguration.SVC_MEC_PARAM)
							  .path(ServiceConfiguration.SVC_MEC_PARAM_UPDATE)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
						      .queryParam(ServiceConfiguration.SVCP_MEC_NAMESPACE, mynamespaceFullPath)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_NAME, paramName)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_NAME_NEW, paramNameNew)
						      .queryParam(ServiceConfiguration.SVCP_MEC_PARAM_TYP, paramType)
							  .type(MediaType.APPLICATION_JSON)
						      .put(Boolean.class, paramRole);
		
		assertEquals(true, b);
	}
	
}
