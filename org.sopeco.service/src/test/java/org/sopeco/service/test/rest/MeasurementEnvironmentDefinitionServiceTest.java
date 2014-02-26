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
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.exchange.ServiceResponse;
import org.sopeco.service.rest.json.CustomObjectMapper;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * The {@link MeasurementEnvironmentDefinitionServiceTest} tests the {@link MeasurementEnvironmentDefinitionService} class.
 * E.g. MED adding, parameter namespace updating...
 * 
 * @author Peter Merkert
 */
public class MeasurementEnvironmentDefinitionServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementEnvironmentDefinitionServiceTest.class.getName());

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public MeasurementEnvironmentDefinitionServiceTest() {
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
	 * Sets the MeasurementEnvironmentDefinition of a scenario to a blank new one.
	 * 
	 * 1. log in
	 * 2. set blank MED
	 */
	@Test
	public void testBlankMED() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// blank the MeasurementEnvironmentDefinition
		r = target().path(ServiceConfiguration.SVC_MED)
				    .path(ServiceConfiguration.SVC_MED_SET)
				    .path(ServiceConfiguration.SVC_MED_SET_BLANK)
				    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
				    .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
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
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// return the MED for the current user
		r = target().path(ServiceConfiguration.SVC_MED)
			  	    .path(ServiceConfiguration.SVC_MED_CURRENT)
			  	    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			  	    .request(MediaType.APPLICATION_JSON)
			  	    .get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());

		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
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
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// return the MED for the current user
		r = target().path(ServiceConfiguration.SVC_MED)
				    .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				    .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
				    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				    .request(MediaType.APPLICATION_JSON)
				    .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// return the MED for the current user
		r = target().path(ServiceConfiguration.SVC_MED)
				 	.path(ServiceConfiguration.SVC_MED_CURRENT)
				 	.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				 	.request(MediaType.APPLICATION_JSON)
				 	.get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());

		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
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
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// create the namespace
		target().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				  .request(MediaType.APPLICATION_JSON)
			      .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// return the MED for the current user
		r = target().path(ServiceConfiguration.SVC_MED)
				    .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				    .path(ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
				    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				    .request(MediaType.APPLICATION_JSON)
				    .delete();
		
		// removal must succeed
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
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
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// create the namespace, to ensure to have at least this one
		target().path(ServiceConfiguration.SVC_MED)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				  .path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			      .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			      .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				     .request(MediaType.APPLICATION_JSON)
			      .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		r = target().path(ServiceConfiguration.SVC_MED)
				    .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				    .path(ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
				    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW, mynamespaceNewName)
				    .request(MediaType.APPLICATION_JSON)
				    .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// the renaming must succeed
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// return the MED for the current user
		r = target().path(ServiceConfiguration.SVC_MED)
				    .path(ServiceConfiguration.SVC_MED_CURRENT)
				    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    .request(MediaType.APPLICATION_JSON)
				    .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());

		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals(mynamespaceNewName, med.getRoot().getChildren().get(0).getName());
		assertEquals("root" + "." + mynamespaceNewName, med.getRoot().getChildren().get(0).getFullName());
		
		
		// test not valid token
		r = target().path(ServiceConfiguration.SVC_MED)
			        .path(ServiceConfiguration.SVC_MED_NAMESPACE)
			        .path(ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
			        .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, "123")
			        .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			        .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW, mynamespaceNewName)
			        .request(MediaType.APPLICATION_JSON)
			        .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
			
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
		
		// test not available namespace (delete once for safety)
		target().path(ServiceConfiguration.SVC_MED)
			    .path(ServiceConfiguration.SVC_MED_NAMESPACE)
			    .path(ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
			    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			    .request(MediaType.APPLICATION_JSON)
			    .delete();
		
		r = target().path(ServiceConfiguration.SVC_MED)
			  	 	.path(ServiceConfiguration.SVC_MED_NAMESPACE)
			  	 	.path(ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
			  	 	.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			  	 	.queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			  	 	.queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW, mynamespaceNewName)
			  	 	.request(MediaType.APPLICATION_JSON)
			  	 	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));

		assertEquals(false, Status.OK.getStatusCode() == r.getStatus());
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
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// create the namespace, to ensure to have at least this one
		target().path(ServiceConfiguration.SVC_MED)
			  	.path(ServiceConfiguration.SVC_MED_NAMESPACE)
			  	.path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			  	.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			  	.queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			  	.request(MediaType.APPLICATION_JSON)
			  	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		r = target().path(ServiceConfiguration.SVC_MED)
				    .path(ServiceConfiguration.SVC_MED_PARAM)
				    .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
				    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				    .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
				    .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
				    .request(MediaType.APPLICATION_JSON)
				    .put(Entity.entity(paramRole, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
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
		String accountname 			= TestConfiguration.TESTACCOUNTNAME;
		String password 			= TestConfiguration.TESTPASSWORD;
		String mynamespace 			= "mynamespacepath";
		String mynamespaceFullPath 	= "root/" + mynamespace;
		String paramName 			= "myparam";
		String paramNameNew 		= "mynewparam";
		String paramType 			= "myparamtype"; // be aware, after setting this is uppercase in the application
		ParameterRole paramRole 	= ParameterRole.INPUT;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// create the namespace, to ensure to have at least this one
		target().path(ServiceConfiguration.SVC_MED)
			  	.path(ServiceConfiguration.SVC_MED_NAMESPACE)
			  	.path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
			  	.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			  	.queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			  	.request(MediaType.APPLICATION_JSON)
			  	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		target().path(ServiceConfiguration.SVC_MED)
			    .path(ServiceConfiguration.SVC_MED_PARAM)
			    .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
			    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			    .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
			    .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
			    .request(MediaType.APPLICATION_JSON)
			    .put(Entity.entity(paramRole, MediaType.APPLICATION_JSON));

		r = target().path(ServiceConfiguration.SVC_MED)
							  					  .path(ServiceConfiguration.SVC_MED_PARAM)
							  					  .path(ServiceConfiguration.SVC_MED_PARAM_UPDATE)
							  					  .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
							  					  .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
							  					  .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
							  					  .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME_NEW, paramNameNew)
							  					  .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
												     .request(MediaType.APPLICATION_JSON)
							  					  .put(Entity.entity(paramRole, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
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
		String accountname 			= TestConfiguration.TESTACCOUNTNAME;
		String password 			= TestConfiguration.TESTPASSWORD;
		String mynamespace 			= "mynamespacepath";
		String mynamespaceFullPath 	= "root/" + mynamespace;
		String paramName 			= "myparam";
		String paramType 			= "myparamtype"; // be aware, after setting this is uppercase
		ParameterRole paramRole 	= ParameterRole.INPUT;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request(MediaType.APPLICATION_JSON)
						     .get();
						
		String token = r.readEntity(String.class);
		
		// create a scenario
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
		
		// create the namespace, to ensure to have at least this one
		target().path(ServiceConfiguration.SVC_MED)
		  		.path(ServiceConfiguration.SVC_MED_NAMESPACE)
		  		.path(ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
		  		.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
		  		.queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
		  		.request(MediaType.APPLICATION_JSON)
		  		.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		target().path(ServiceConfiguration.SVC_MED)
			    .path(ServiceConfiguration.SVC_MED_PARAM)
			    .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
			    .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			    .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
			    .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
			    .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
			    .request(MediaType.APPLICATION_JSON)
			    .put(Entity.entity(paramRole, MediaType.APPLICATION_JSON));

		r = target().path(ServiceConfiguration.SVC_MED)
					.path(ServiceConfiguration.SVC_MED_PARAM)
					.path(ServiceConfiguration.SVC_MED_PARAM_REMOVE)
					.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
					.queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
					.queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
					.request(MediaType.APPLICATION_JSON)
		      .		delete();
		
		// deletion must have been succesful
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
	}
}
