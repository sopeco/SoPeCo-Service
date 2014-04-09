/**
 * Copyright (c) 2014 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import org.sopeco.service.rest.MeasurementSpecificationService;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The {@link MeasurementSpecServiceTest} tests various features of the
 * {@link MeasurementSpecificationService} RESTful services.
 * 
 * @author Peter Merkert
 */
public class MeasurementSpecServiceTest extends AbstractServiceTest {

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * Checks if it is possible to have two MeasurementSpecification
	 * with the same name. This should not be possible.
	 */
	@Test
	public void testMeasurementSpecNameListing() {
		if (skipTests) return;
		
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
		
		Response r = target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
							 .path(TEST_SCENARIO_NAME)
							 .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_LIST)
							 .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
							 .request(MediaType.APPLICATION_JSON)
							 .get();

		List<String> list = r.readEntity(new GenericType<List<String>>() { });

		assertEquals(true, list != null);
		assertEquals(true, list.size() >= 1);
		assertEquals(true, list.contains(TEST_MEASUREMENT_SPECIFICATION_NAME));
		
		// nwo create two more specifications
		target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
		 		.path(TEST_SCENARIO_NAME)
		        .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_CREATE)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME, measurementSpecName2)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
				.path(TEST_SCENARIO_NAME)
		        .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_CREATE)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME, measurementSpecName3)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		r = target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
					.path(TEST_SCENARIO_NAME)
       	    	    .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_LIST)
       	    	    .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
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
	 * 3. create measurementspecification
	 * 4. create measurementspecification (with same name as in step 4)
	 */
	@Test
	public void testMeasurementSpecNameDoubleAdding() {
		if (skipTests) return;
		
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

		
		// now create a new measurement spec for the user once
		target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
				.path(TEST_SCENARIO_NAME)
		        .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_CREATE)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
		        .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		//create it now a second time, this must fail
		Response r = target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
							 .path(TEST_SCENARIO_NAME)
							 .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_CREATE)
							 .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
							 .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
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
	 * 3. create new measurementspecification
	 * 4. rename current selected measurementspecification
	 */
	@Test
	public void testMeasurementSpecSwitchWorkingSpec() {
		if (skipTests) return;
		
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
		
		// now create the measurement spec for the user once
		target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
	          .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_CREATE)
	          .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
	          .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			  .request(MediaType.APPLICATION_JSON)
			  .post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// rename the current selected measurementspecification
		Response r = target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
							 .path(TEST_SCENARIO_NAME)
							 .path(TEST_MEASUREMENT_SPECIFICATION_NAME)
							 .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_RENAME)
							 .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
							 .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME, newMeasurementSpecName)
							 .request(MediaType.APPLICATION_JSON)
							 .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// the renaming should work fine
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// now lookup the name we just added
		r = target().path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
					.path(TEST_SCENARIO_NAME)
	         	    .path(ServiceConfiguration.SVC_MEASUREMENTSPEC_LIST)
	         	    .queryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN, token)
	         	    .request(MediaType.APPLICATION_JSON)
	         	    .get();
		

		List<String> list = r.readEntity(new GenericType<List<String>>() { });

		assertEquals(true, list != null);
		assertEquals(false, list.contains(TEST_MEASUREMENT_SPECIFICATION_NAME));
		assertEquals(true,  list.contains(newMeasurementSpecName));
		
		logout(token);
	}
	
}
