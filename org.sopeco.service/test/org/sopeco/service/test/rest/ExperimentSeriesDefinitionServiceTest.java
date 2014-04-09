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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.ExperimentSeriesDefinitionService;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The {@link ExperimentSeriesDefinitionServiceTest} tests various features of the
 * {@link ExperimentSeriesDefinitionService} RESTful services.
 * 
 * @author Peter Merkert
 */
public class ExperimentSeriesDefinitionServiceTest extends AbstractServiceTest {

	/**
	 * The TEST_SCENARIO_NAME is used to shorten the access to the test variable for the scenario name.
	 */
	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	
	/**
	 * The TEST_MEASUREMENT_SPECIFICATION_NAME is used to shorten the access to the test variable meas. spec. name.
	 */
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The TEST_MEASUREMENT_SPECIFICATION_NAME is used to shorten the access to the test variable meas. spec. name.
	 */
	private static final String TEST_ESD_NAME = TestConfiguration.TEST_ESD_NAME;
	
	/**
	 * 1. login
	 * 2. create scenario
	 * 3. test if there is exactly one ESD in the list of all ESDs
	 */
	@Test
	public void testGetAllExperimentSeriesDefinition() {
		if (skipTests) return;
		
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		String token = login(accountname, password);
		
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		esd.setName(TEST_ESD_NAME);
		createScenario(TEST_SCENARIO_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME, esd, token);
		
		Response r = target().path(ServiceConfiguration.SVC_ESD)
							 .path(TEST_SCENARIO_NAME)
							 .path(TEST_MEASUREMENT_SPECIFICATION_NAME)
							 .queryParam(ServiceConfiguration.SVCP_ESD_TOKEN, token)
							 .request(MediaType.APPLICATION_JSON)
							 .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		List<ExperimentSeriesDefinition> listESD = r.readEntity(new GenericType<List<ExperimentSeriesDefinition>>() { });
		
		// the list should only contain EXACTLY one ESD
		assertEquals(1, listESD.size());
		assertEquals(TEST_ESD_NAME, listESD.get(0).getName());
		
		logout(token);
	}
	
	/**
	 * 1. login
	 * 2. create scenario
	 * 3. fetch directly the {@link ExperimentSeriesDefinition} via the name
	 */
	@Test
	public void testGetExperimentSeriesDefinition() {
		if (skipTests) return;
		
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		String token = login(accountname, password);
		
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		esd.setName(TEST_ESD_NAME);
		createScenario(TEST_SCENARIO_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME, esd, token);
		
		Response r = target().path(ServiceConfiguration.SVC_ESD)
							 .path(TEST_SCENARIO_NAME)
							 .path(TEST_MEASUREMENT_SPECIFICATION_NAME)
							 .path(TEST_ESD_NAME)
							 .queryParam(ServiceConfiguration.SVCP_ESD_TOKEN, token)
							 .request(MediaType.APPLICATION_JSON)
							 .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		ExperimentSeriesDefinition esdFromService = r.readEntity(ExperimentSeriesDefinition.class);
		
		assertEquals(TEST_ESD_NAME, esdFromService.getName());
		
		logout(token);
	}
	
	/**
	 * 1. login
	 * 2. create scenario
	 * 3. rename ESD
	 * 4. fetch ESD directly with new name
	 */
	@Test
	public void testRenameExperimentSeriesDefinition() {
		if (skipTests) return;
		
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		String newESDName 	= "newESDName";
		
		String token = login(accountname, password);
		
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		esd.setName(TEST_ESD_NAME);
		createScenario(TEST_SCENARIO_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME, esd, token);
		
		Response r = target().path(ServiceConfiguration.SVC_ESD)
							 .path(TEST_SCENARIO_NAME)
							 .path(TEST_MEASUREMENT_SPECIFICATION_NAME)
							 .path(TEST_ESD_NAME)
							 .queryParam(ServiceConfiguration.SVCP_ESD_TOKEN, token)
							 .request(MediaType.APPLICATION_JSON)
							 .post(Entity.entity(newESDName, MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		// now check, if the name was correctly stored in the database
		r = target().path(ServiceConfiguration.SVC_ESD)
					.path(TEST_SCENARIO_NAME)
					.path(TEST_MEASUREMENT_SPECIFICATION_NAME)
					.path(newESDName)
					.queryParam(ServiceConfiguration.SVCP_ESD_TOKEN, token)
					.request(MediaType.APPLICATION_JSON)
					.get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		ExperimentSeriesDefinition esdFromService = r.readEntity(ExperimentSeriesDefinition.class);
		
		assertEquals(newESDName, esdFromService.getName());
		
		logout(token);
	}
	
	/**
	 * 1. login
	 * 2. create scenario
	 * 3. add a new ESD
	 * 4. fetch all ESDs and ccheck amount (must be 2!)
	 */
	@Test
	public void testAddExperimentSeriesDefinition() {
		if (skipTests) return;
		
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		String newESDName 	= "newESDName";
		
		String token = login(accountname, password);
		
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		esd.setName(TEST_ESD_NAME);
		createScenario(TEST_SCENARIO_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME, esd, token);
		
		// now add a 2nd ESD
		ExperimentSeriesDefinition esd2 = new ExperimentSeriesDefinition();
		esd2.setName(newESDName);
		Response r = target().path(ServiceConfiguration.SVC_ESD)
							 .path(TEST_SCENARIO_NAME)
							 .path(TEST_MEASUREMENT_SPECIFICATION_NAME)
							 .queryParam(ServiceConfiguration.SVCP_ESD_TOKEN, token)
							 .request(MediaType.APPLICATION_JSON)
							 .put(Entity.entity(esd2, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
				
		// now check the new amount of ESD
		r = target().path(ServiceConfiguration.SVC_ESD)
				 	.path(TEST_SCENARIO_NAME)
				 	.path(TEST_MEASUREMENT_SPECIFICATION_NAME)
				 	.queryParam(ServiceConfiguration.SVCP_ESD_TOKEN, token)
				 	.request(MediaType.APPLICATION_JSON)
				 	.get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		List<ExperimentSeriesDefinition> listESD = r.readEntity(new GenericType<List<ExperimentSeriesDefinition>>() { });
		
		// the list should only contain EXACTLY one ESD
		assertEquals(2, listESD.size());
		
		logout(token);
	}
}
