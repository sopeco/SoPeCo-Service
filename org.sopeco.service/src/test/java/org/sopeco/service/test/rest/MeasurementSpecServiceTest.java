package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The <code>MeasurementSpecServiceTest</code> tests various features of the
 * <code>MeasurementSpecificationService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class MeasurementSpecServiceTest extends AbstractServiceTest {

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
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
		
		String token = login(accountname, password);
		 
		// add scenario and switch to
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
		
		// switch to the newly created measurmentspecification
		Response r  = target().path(ServiceConfiguration.SVC_MEASUREMENT)
			     		      .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
			     		      .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			     		      .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
			     		      .request(MediaType.APPLICATION_JSON)
			     		      .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
			
		// the switch to the newly created measurmentspecification must go right!
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
			        .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
			        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
					.request(MediaType.APPLICATION_JSON)
			        .get();

		List<String> list = r.readEntity(new GenericType<List<String>>() { });

		assertEquals(true, list != null);
		assertEquals(true, list.size() >= 1);
		assertEquals(true, list.contains(TEST_MEASUREMENT_SPECIFICATION_NAME));
		
		// nwo create two more specifications
		target().path(ServiceConfiguration.SVC_MEASUREMENT)
		        .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName2)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		target().path(ServiceConfiguration.SVC_MEASUREMENT)
		        .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, measurementSpecName3)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
       	    	    .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
       	    	    .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
    				.request(MediaType.APPLICATION_JSON)
       	    	    .get();

		list = r.readEntity(new GenericType<List<String>>() { });

		assertEquals(true, list != null);
		assertEquals(true, list.size() >= measurementSpecCount);
		assertEquals(true, list.contains(measurementSpecName2));
		assertEquals(true, list.contains(measurementSpecName3));
		
		logout(token);
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
		
		String token = login(accountname, password);
		
		// add scenario and switch to
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
		
		// now create a new measurement spec for the user once
		target().path(ServiceConfiguration.SVC_MEASUREMENT)
		        .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// switch to the newly created measurmentspecification
		Response r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
				    	     .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
				    	     .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				    	     .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
				    	     .request(MediaType.APPLICATION_JSON)
				    	     .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// the switch to the newly created measurmentspecification must go right!
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		//create it now a second time, this must fail
		r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
	                .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
	                .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
	                .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// the second addition must fail
		assertEquals(Status.CONFLICT.getStatusCode(), r.getStatus());
		
		logout(token);
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
		
		String token = login(accountname, password);
		
		// add scenario and switch to
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
		
		// now create the measurement spec for the user once
		target().path(ServiceConfiguration.SVC_MEASUREMENT)
	          .path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
	          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
	          .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			  .request(MediaType.APPLICATION_JSON)
			  .post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// switch to the newly created measurmentspecification
		Response r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
						     .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
						     .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
						     .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
						     .request(MediaType.APPLICATION_JSON)
						     .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// rename the current selected measurementspecification
		r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
					.path(ServiceConfiguration.SVC_MEASUREMENT_RENAME)
					.queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
					.queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, newMeasurementSpecName)
					.request(MediaType.APPLICATION_JSON)
					.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// the renaming should work fine
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// now lookup the name we just added
		r = target().path(ServiceConfiguration.SVC_MEASUREMENT)
	         	    .path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
	         	    .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
	         	    .request(MediaType.APPLICATION_JSON)
	         	    .get();
		

		List<String> list = r.readEntity(new GenericType<List<String>>() { });

		assertEquals(true, list != null);
		assertEquals(false, list.contains(TEST_MEASUREMENT_SPECIFICATION_NAME));
		assertEquals(true,  list.contains(newMeasurementSpecName));
		
		logout(token);
	}
	
}
