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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.engine.model.ScenarioDefinitionReader;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.ScenarioService;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The {@link ScenarioServiceTest} tests various features of the
 * {@link ScenarioService} RESTful services.
 * 
 * @author Peter Merkert
 */
public class ScenarioServiceTest extends AbstractServiceTest {

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public ScenarioServiceTest() {
		super();
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
		if (skipTests) return;
		
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;

		String token = login(accountname, password);
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we check the double adding afterwards)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .request(MediaType.APPLICATION_JSON)
				  
				  .post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		// now add scenario WITH THE SAME NAME a second time
		Response r = target().path(ServiceConfiguration.SVC_SCENARIO)
				  			 .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  			 .path(TEST_SCENARIO_NAME)
				  			 .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  			 .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  			 .request(MediaType.APPLICATION_JSON)
				  			 .post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.CONFLICT.getStatusCode(), r.getStatus());
		
		logout(token);
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
		if (skipTests) return;
		
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		String token = login(accountname, password);
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we don't care about this issue here)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
			  	.path(ServiceConfiguration.SVC_SCENARIO_ADD)
			  	.path(TEST_SCENARIO_NAME)
			  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			  	.request(MediaType.APPLICATION_JSON)
			  	.post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		// now check if at least one scenario is in the list
		Response r = target().path(ServiceConfiguration.SVC_SCENARIO)
				  			 .path(ServiceConfiguration.SVC_SCENARIO_LIST)
				  			 .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  			 .request(MediaType.APPLICATION_JSON)
				  			 .get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		String[] list = r.readEntity(String[].class);
		
		assertEquals(true, list.length > 0);
		
		logout(token);
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
		if (skipTests) return;
		
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;

		String token = login(accountname, password);
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we don't care here about it)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
			  	.path(ServiceConfiguration.SVC_SCENARIO_ADD)
			  	.path(TEST_SCENARIO_NAME)
			  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			  	.request(MediaType.APPLICATION_JSON)
			  	.post(Entity.entity(esd, MediaType.APPLICATION_JSON));

		// now try to delete the scenario
		Response r = target().path(ServiceConfiguration.SVC_SCENARIO)
			  				 .path(TEST_SCENARIO_NAME)
			  				 .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
			  				 .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			  				 .request(MediaType.APPLICATION_JSON)
			  				 .delete();
		
		// the deletion should be successful completed
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		logout(token);
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
		if (skipTests) return;
		
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		String token = login(accountname, password);
		
		target().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_DELETE)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			    .request(MediaType.APPLICATION_JSON)
				  .delete();
		
		// add a default scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
			  	.path(ServiceConfiguration.SVC_SCENARIO_ADD)
			  	.path(TEST_SCENARIO_NAME)
			  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
			  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			  	.request(MediaType.APPLICATION_JSON)
			  	.post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		// now fetch the scenario data from the RESTful service
		Response r = target().path(ServiceConfiguration.SVC_SCENARIO)
				 			 .path(TEST_SCENARIO_NAME)
							 .path(ServiceConfiguration.SVC_SCENARIO_XML)
							 .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							 .request(MediaType.APPLICATION_JSON)
							 .get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		String xml = r.readEntity(String.class);
		
		r = target().path(ServiceConfiguration.SVC_MED)
	 			 	.path(TEST_SCENARIO_NAME)
				 	.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				 	.request(MediaType.APPLICATION_JSON)
				 	.get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
		ScenarioDefinitionReader sdr = new ScenarioDefinitionReader(med, token);
		ScenarioDefinition scenarioDefinitionXML = sdr.readFromString(xml);
		
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
			  		.path(TEST_SCENARIO_NAME)
			     	.path(ServiceConfiguration.SVC_SCENARIO_DEFINITON)
			     	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			     	.request(MediaType.APPLICATION_JSON)
			     	.get();
		
		ScenarioDefinition sd = r.readEntity(ScenarioDefinition.class);
		
		assertEquals(true, sd.equals(scenarioDefinitionXML));
		
		logout(token);
	}
}
