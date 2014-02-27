package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.MeasurementEnvironmentDefinitionService;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The {@link MeasurementEnvironmentDefinitionServiceTest} tests the {@link MeasurementEnvironmentDefinitionService} class.
 * E.g. MED adding, parameter namespace updating...
 * 
 * @author Peter Merkert
 */
public class MeasurementEnvironmentDefinitionServiceTest extends AbstractServiceTest {

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
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

		String token = login(accountname, password);
		
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
		Response r = target().path(ServiceConfiguration.SVC_MED)
				             .path(ServiceConfiguration.SVC_MED_SET)
				             .path(ServiceConfiguration.SVC_MED_SET_BLANK)
				             .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				             .request(MediaType.APPLICATION_JSON)
				             .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
		
		logout(token);
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
		
		String token = login(accountname, password);
		
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
		Response r = target().path(ServiceConfiguration.SVC_MED)
			  	             .path(ServiceConfiguration.SVC_MED_CURRENT)
			  	             .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
			  	             .request(MediaType.APPLICATION_JSON)
			  	             .get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());

		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
		
		logout(token);
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

		String token = login(accountname, password);
		
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
		Response r = target().path(ServiceConfiguration.SVC_MED)
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
		
		logout(token);
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

		String token = login(accountname, password);
		
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
		Response r = target().path(ServiceConfiguration.SVC_MED)
				    		 .path(ServiceConfiguration.SVC_MED_NAMESPACE)
				    		 .path(ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
				    		 .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				    		 .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
				    		 .request(MediaType.APPLICATION_JSON)
				    		 .delete();
		
		// removal must succeed
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
	
		logout(token);
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
		
		String token = login(accountname, password);
		
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
		
		Response r = target().path(ServiceConfiguration.SVC_MED)
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
		
		logout(token);
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
		
		String token = login(accountname, password);
		
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
		
		Response r = target().path(ServiceConfiguration.SVC_MED)
						     .path(ServiceConfiguration.SVC_MED_PARAM)
						     .path(ServiceConfiguration.SVC_MED_PARAM_ADD)
						     .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
						     .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
						     .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
						     .queryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP, paramType)
						     .request(MediaType.APPLICATION_JSON)
						     .put(Entity.entity(paramRole, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		logout(token);
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
		
		String token = login(accountname, password);
		
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

		Response r = target().path(ServiceConfiguration.SVC_MED)
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
		
		logout(token);
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
		
		String token = login(accountname, password);
		
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

		Response r = target().path(ServiceConfiguration.SVC_MED)
							 .path(ServiceConfiguration.SVC_MED_PARAM)
							 .path(ServiceConfiguration.SVC_MED_PARAM_REMOVE)
							 .queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
							 .queryParam(ServiceConfiguration.SVCP_MED_NAMESPACE, mynamespaceFullPath)
							 .queryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME, paramName)
							 .request(MediaType.APPLICATION_JSON)
							 .delete();
		
		// deletion must have been succesful
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		logout(token);
	}
}
