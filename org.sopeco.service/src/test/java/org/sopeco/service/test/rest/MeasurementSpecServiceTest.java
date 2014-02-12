package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.exchange.ServiceResponse;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * The <code>MeasurementSpecServiceTest</code> tests various features of the
 * <code>MeasurementSpecificationService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class MeasurementSpecServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementSpecServiceTest.class.getName());

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public MeasurementSpecServiceTest() {
		super();
	}

	/**
	 * Configure is called on the object creation of a JerseyTest. It's used to
	 * configure where the JerseyTest can find the REST services.
	 * 
	 * @return the configuration
	 */
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(TestConfiguration.PACKAGE_NAME_REST)
								   .clientConfig(createClientConfig())
								   .build();
	}
	
	/**
	 * Sets the client config for the client. The method is only used
	 * to give the possiblity to adjust the ClientConfig.
	 * 
	 * This method is called by {@link configure()}.
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new DefaultClientConfig();
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
	 * Checks if it is possible to register an account twice.
	 * 
	 * 1. log in
	 * 2. adds new scenario
	 * 3. switch to newly created scenario
	 * 4.
	 */
	@Test
	public void testMeasurementSpecNameListing() {
		String accountname 				= TestConfiguration.TESTACCOUNTNAME;
		String password 				= TestConfiguration.TESTPASSWORD;
		String measurementSpecName2 	= "examplespecname2";
		String measurementSpecName3 	= "examplespecname3";
		final int measurementSpecCount 	= 3;
		
		// just create the account once to be sure it already exists
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });

		String token = sr.getObject();
		 
		// add at least the examplescenario for ensurance that there is an measurementSpec available
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);

		// switch to scenario (if not already in this scenario)
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// switch to the newly created measurmentspecification
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
												  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
												  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
												  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
												  .accept(MediaType.APPLICATION_JSON)
												  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// the switch to the newly created measurmentspecification must go right!
		assertEquals(true, sr_b.getObject());
		
		ServiceResponse<List<String>> sr_measurementList = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
															         .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
															         .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
															         .get(new GenericType<ServiceResponse<List<String>>>() { });

		assertEquals(true, sr_measurementList.getObject().size() >= 1);
		assertEquals(true, sr_measurementList.getObject().contains(TEST_MEASUREMENT_SPECIFICATION_NAME));
		
		// nwo create two more specifications
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName2)
		          .post(new GenericType<ServiceResponse<Boolean>>() { });
		
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName3)
		          .post(new GenericType<ServiceResponse<Boolean>>() { });
		
		sr_measurementList = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
					       	    	   .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
					       	    	   .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
					       	    	   .get(new GenericType<ServiceResponse<List<String>>>() { });

		assertEquals(true, sr_measurementList.getObject().size() >= measurementSpecCount);
		assertEquals(true, sr_measurementList.getObject().contains(measurementSpecName2));
		assertEquals(true, sr_measurementList.getObject().contains(measurementSpecName3));
	}

	/**
	 * Tests adding a MeasurementSpecification with the same name twice.
	 * 
	 * 1. login
	 * 2. add scenario
	 * 3. switch scenario
	 * 4. create measurementspecification
	 * 5. switch measurementspecification
	 * 6. create measurementspecification (with same name as in step 4)
	 */
	@Test
	public void testMeasurementSpecNameDoubleAdding() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// just create the account once to be sure it already exists
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											  .get(new GenericType<ServiceResponse<String>>() { });

		String token = sr.getObject();
		
		// add at least the examplescenario for ensurance that there is an measurementSpec available
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		// switch to scenario (if not already in this scenario)
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// now create a new measurement spec for the user once
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
		          .post(new GenericType<ServiceResponse<Boolean>>() { });
		
		// switch to the newly created measurmentspecification
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
							  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// the switch to the newly created measurmentspecification must go right!
		assertEquals(true, sr_b.getObject());
		
		//create it now a second time, this must fail
		sr_b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
			             .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
			             .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			             .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			             .post(new GenericType<ServiceResponse<Boolean>>() { });
		
		// the second addition must fail
		assertEquals(false, sr_b.getObject());
	}
	
	/**
	 * This test does the following:
	 * 
	 * 1. login
	 * 2. adds scenario
	 * 3. switch to newly created scenario
	 * 4. create new measurementspecification
	 * 5. switch to newly created measurementspecification
	 * 6. rename current selected measurementspecification
	 */
	@Test
	public void testMeasurementSpecSwitchWorkingSpec() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String newMeasurementSpecName = "newMeasurementSpecificationName";
		
		// just create the account once to be sure it already exists
		ServiceResponse<String> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											   .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
											   .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
											   .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
											   .get(new GenericType<ServiceResponse<String>>() { });

		String token = sr.getObject();
		
		// add at least the examplescenario for ensurance that there is an measurementSpec available
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(new GenericType<ServiceResponse<Boolean>>() { }, esd);
		
		// switch to scenario (if not already in this scenario)
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// now create the measurement spec for the user once
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
		          .post(new GenericType<ServiceResponse<Boolean>>() { });
		
		// switch to the newly created measurmentspecification
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
						  						  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
						  						  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
						  						  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
						  						  .accept(MediaType.APPLICATION_JSON)
						  						  .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// rename the current selected measurementspecification
		sr_b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
			             .path(ServiceConfiguration.SVC_MEASUREMENT_RENAME)
			             .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			             .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, newMeasurementSpecName)
			             .put(new GenericType<ServiceResponse<Boolean>>() { });
		
		// the renaming should work fine
		assertEquals(true, sr_b.getObject());
		
		// now lookup the name we just added
		ServiceResponse<List<String>> sr_measurementList = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
														         	 .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
														         	 .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
														         	 .get(new GenericType<ServiceResponse<List<String>>>() { });
		
		assertEquals(false, sr_measurementList.getObject().contains(TEST_MEASUREMENT_SPECIFICATION_NAME));
		assertEquals(true,  sr_measurementList.getObject().contains(newMeasurementSpecName));
	}
	
}
