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
import org.sopeco.engine.model.ScenarioDefinitionReader;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectMapper;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * The <code>ScenarioServiceTest</code> tests various features of the
 * <code>ScenarioService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class ScenarioServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioServiceTest.class);

	private static final String TEST_SCENARIO_NAME = TestConfiguration.TEST_SCENARIO_NAME;
	private static final String TEST_MEASUREMENT_SPECIFICATION_NAME = TestConfiguration.TEST_MEASUREMENT_SPECIFICATION_NAME;
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public ScenarioServiceTest() {
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
						     .get();;
						
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
	 * Try adding two scenarios with the same name. The second addition must fail.
	 * 
	 * 1. log in
	 * 2. add new scenario with name X
	 * 3. add again new scenario with name X
	 */
	@Test
	public void testScenarioDoubleAdd() {
		// connect to test users account
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
						     .get();
						
		String token = r.readEntity(String.class);
		
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
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
				  	.path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  	.path(TEST_SCENARIO_NAME)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  	.request(MediaType.APPLICATION_JSON)
				  	.post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		assertEquals(Status.CONFLICT.getStatusCode(), r.getStatus());
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
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
						     .get();
						
		String token = r.readEntity(String.class);
		
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
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
				  	.path(ServiceConfiguration.SVC_SCENARIO_LIST)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  	.request(MediaType.APPLICATION_JSON)
				  	.get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		String[] list = r.readEntity(String[].class);
		
		assertEquals(true, list.length > 0);
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
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
						     .get();
						
		String token = r.readEntity(String.class);
		
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
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
			  		.path(TEST_SCENARIO_NAME)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  	.request(MediaType.APPLICATION_JSON)
				  	.delete();
		
		// the deletion should be successful completed
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
	}

	/**
	 * Try to switch to a newly created scenario
	 * 
	 * 1. log in
	 * 2. add scenario
	 * 3. switch scenario
	 */
	@Test
	public void testScenarioSwitch() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
						     .get();
						
		String token = r.readEntity(String.class);
		
		// add a default scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		target().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path(TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .request(MediaType.APPLICATION_JSON)
				  
				  .post(Entity.entity(esd, MediaType.APPLICATION_JSON));
		
		// now try to switch the scenario
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
				  	.path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  	.path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  	.request(MediaType.APPLICATION_JSON)
				  	.put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// the switch should be succesful
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
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
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
						     .request()
						     .get();
						
		String token = r.readEntity(String.class);
		
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
		
		target().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, TEST_SCENARIO_NAME)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .request(MediaType.APPLICATION_JSON)
				  .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		// switch to the newly created measurmentspecification
		target().path(ServiceConfiguration.SVC_MEASUREMENT)
				  .path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_NAME, TEST_MEASUREMENT_SPECIFICATION_NAME)
				  .queryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN, token)
				  .request(MediaType.APPLICATION_JSON)
				  .put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
		
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
				   	.path(ServiceConfiguration.SVC_SCENARIO_XML)
				   	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				   	.request(MediaType.APPLICATION_JSON)
				   	.get();
		
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		String xml = r.readEntity(String.class);
		
		r = target().path(ServiceConfiguration.SVC_MED)
				 	.path(ServiceConfiguration.SVC_MED_CURRENT)
				 	.queryParam(ServiceConfiguration.SVCP_MED_TOKEN, token)
				 	.request(MediaType.APPLICATION_JSON)
				 	.get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		
		MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
		
		ScenarioDefinitionReader sdr = new ScenarioDefinitionReader(med, token);
		ScenarioDefinition scenarioDefinitionXML = sdr.readFromString(xml);
		
		r = target().path(ServiceConfiguration.SVC_SCENARIO)
			     	.path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
			     	.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
			     	.request(MediaType.APPLICATION_JSON)
			     	.get();
		
		ScenarioDefinition sd = r.readEntity(ScenarioDefinition.class);
		
		assertEquals(true, sd.equals(scenarioDefinitionXML));
	}
}
