package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.shared.Message;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class MeasurementSpecServiceTest extends JerseyTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementSpecServiceTest.class.getName());
	
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
	 * Checks if it is possible to register an account twice.
	 * 
	 * 1. log in
	 * 2. adds new scenario
	 * 3. switch to newly created scenario
	 * 4.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testMeasurementSpecNameListing() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String scenarioname = "examplescenario";
		String measurementSpecName = "examplespecname";
		String measurementSpecName2 = "examplespecname2";
		String measurementSpecName3 = "examplespecname3";
		
		// just create the account once to be sure it already exists
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		 
		// add at least the examplescenario for ensurance that there is an measurementSpec available
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, measurementSpecName)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);

		// switch to scenario (if not already in this scenario)
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// switch to the newly created measurmentspecification
		Boolean b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
							  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(Boolean.class);
		
		// the switch to the newly created measurmentspecification must go right!
		assertEquals(true, b);
		
		List<String> measurementList = (List<String>)resource().path(ServiceConfiguration.SVC_MEASUREMENT)
														       .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
															   .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
														       .get(List.class);

		assertEquals(true, measurementList.size() >= 1);
		assertEquals(true, measurementList.contains(measurementSpecName));
		
		// nwo create two more specifications
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName2)
		          .post(Boolean.class);
		
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName3)
		          .post(Boolean.class);
		
		measurementList = (List<String>)resource().path(ServiceConfiguration.SVC_MEASUREMENT)
			       .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
				   .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			       .get(List.class);

		assertEquals(true, measurementList.size() >= 3);
		assertEquals(true, measurementList.contains(measurementSpecName2));
		assertEquals(true, measurementList.contains(measurementSpecName3));
	}

	@Test
	public void testMeasurementSpecNameDoubleAdding() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		String scenarioname = "examplescenario";
		String measurementspecname = "measurementspecexample";
		
		// just create the account once to be sure it already exists
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// add at least the examplescenario for ensurance that there is an measurementSpec available
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// switch to scenario (if not already in this scenario)
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// now create a new measurement spec for the user once
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementspecname)
		          .post(Boolean.class);
		
		// switch to the newly created measurmentspecification
		Boolean b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
							  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementspecname)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(Boolean.class);
		
		// the switch to the newly created measurmentspecification must go right!
		assertEquals(true, b);
		
		//create it now a second time, this must fail
		b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
			          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
			          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementspecname)
			          .post(Boolean.class);
		
		// the second addition must fail
		assertEquals(false, b);
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
		String scenarioname = "examplescenario";
		String measurementSpecName = "measurementspecexample";
		String newMeasurementSpecName = "newmeasurementspecexample";
		
		// just create the account once to be sure it already exists
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// add at least the examplescenario for ensurance that there is an measurementSpec available
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// switch to scenario (if not already in this scenario)
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioname)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// now create the measurement spec for the user once
		resource().path(ServiceConfiguration.SVC_MEASUREMENT)
		          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName)
		          .post(Boolean.class);
		
		// switch to the newly created measurmentspecification
		Boolean b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
							  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName)
							  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .put(Boolean.class);
		
		// rename the current selected measurementspecification
		b = resource().path(ServiceConfiguration.SVC_MEASUREMENT)
			          .path(ServiceConfiguration.SVC_MEASUREMENT_RENAME)
			          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, newMeasurementSpecName)
			          .put(Boolean.class);
		
		// the renaming should work fine
		assertEquals(true, b);
		
		// now lookup the name we just added
		@SuppressWarnings("unchecked")
		List<String> measurementList = (List<String>)resource().path(ServiceConfiguration.SVC_MEASUREMENT)
														       .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
															   .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
														       .get(List.class);
		
		assertEquals(true, measurementList.contains(measurementSpecName));
	}
	
}
