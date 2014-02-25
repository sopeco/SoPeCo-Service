package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.validation.constraints.Null;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
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
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.StartUpService;
import org.sopeco.service.rest.exchange.MECStatus;
import org.sopeco.service.rest.json.CustomObjectMapper;
import org.sopeco.service.test.configuration.TestConfiguration;
import org.sopeco.service.test.rest.fake.TestMEC;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;


/**
 * The {@link MeasurementControllerServiceTest} tests the {@link MeasurementControllerService} class.
 * The {@link ServletContextListener} of class {@link StartUpService} is connected to the test container
 * and starts the ServerSocket.
 * <br />
 * <br />
 * The ServerSocket is started on each test, because the testcontainer is always shut down. This is not
 * a bug, but a feature in JerseyTest. A ticket has been opened with the issue
 * <code>https://java.net/jira/browse/JERSEY-705</code>.
 * 
 * @author Peter Merkert
 */
public class MeasurementControllerServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementControllerServiceTest.class.getName());
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public MeasurementControllerServiceTest() {
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
					      	 .request(MediaType.APPLICATION_JSON)
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
	 * Tests the Status of a MEC. This test only tests and invalid status
	 * and not if the status of a registered MEC is returned correctly.
	 * 
	 * 1. log in
	 * 2. get MEC status (invalid URL)
	 * 3. get MEC status (invalid token)
	 */
	@Test
	public void testMECStatus() {
		LOGGER.debug("Testing fetch of MEC status.");
		
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
			     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
			     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
			     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
			     .request(MediaType.APPLICATION_JSON)
			     .get();
			
		String token = r.readEntity(String.class);
		
		// connect to a random string
		r = target().path(ServiceConfiguration.SVC_MEC)
			    	.path(ServiceConfiguration.SVC_MEC_STATUS)
			    	.queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			    	.queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
			    	.request(MediaType.APPLICATION_JSON)
			    	.get();
					
		MECStatus mecstatus = r.readEntity(MECStatus.class);
		
		assertEquals(true, mecstatus != null);
		assertEquals(MECStatus.NO_VALID_MEC_URL, mecstatus.getStatus());
	
		// check if a wrong token fails, too
		r = target().path(ServiceConfiguration.SVC_MEC)
		      	 	.path(ServiceConfiguration.SVC_MEC_STATUS)
		      	 	.queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, "myrandomtoken")
		      	 	.queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
		      	 	.request(MediaType.APPLICATION_JSON)
		      	 	.get();

		assertEquals(Status.UNAUTHORIZED.getStatusCode(), r.getStatus());
	}
	
	/**
	 * Tests the Status of a correct registered MEC. If this method fails, the whole MEC
	 * registration does not work properly. A fake MEC (<code>TestMEC</code>) is started up,
	 * which registers 3 controllers on a custom URL (more information in the class of the
	 * TestMEC).<br />
	 * The connection to this controller is checked.
	 * 
	 * 1. log in
	 * 2. startup the TestMEC
	 * 3. request MEC status
	 */
	@Test
	public void testMECStatusValidController() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		String socketURI 	= "socket://" + TestMEC.MEC_ID + "/" + TestMEC.MEC_SUB_ID_1;
		
		// log into the account
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
			     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
			     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
			     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
		      	 	.request(MediaType.APPLICATION_JSON)
			     .get();
			
		String token = r.readEntity(String.class);

		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		// now the controller status is requested
		r = target().path(ServiceConfiguration.SVC_MEC)
			        .path(ServiceConfiguration.SVC_MEC_STATUS)
			        .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			        .queryParam(ServiceConfiguration.SVCP_MEC_URL, socketURI)
		      	 	.request(MediaType.APPLICATION_JSON)
			        .get();
		
		MECStatus mecstatus = r.readEntity(MECStatus.class);

		assertEquals(true, mecstatus != null);
		assertEquals(MECStatus.STATUS_ONLINE, mecstatus.getStatus());
		
	}
	
	/**
	 * Tests the listing of a correct registered MEC. <br />
	 * The service is requested to listed all the controllers connected with a custom MEC ID.
	 * The list must contain the MEC names, which connection was established in the
	 * <code>TestMEC</code> class.
	 * 
	 * 1. log in
	 * 2. get MEC list
	 */
	@Test
	public void testMECGetControllerList() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		String mecID 		= TestMEC.MEC_ID;
		
		// log into the account
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
			     .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
			     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
			     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
		      	 	.request(MediaType.APPLICATION_JSON)
			     .get();
			
		String token = r.readEntity(String.class);

		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		// now the controller status is requested
		r = target().path(ServiceConfiguration.SVC_MEC)
			        .path(ServiceConfiguration.SVC_MEC_LIST)
			        .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
			        .queryParam(ServiceConfiguration.SVCP_MEC_ID, mecID)
		      	 	.request(MediaType.APPLICATION_JSON)
			        .get();
		
		List<String> list = r.readEntity(new GenericType<List<String>>() { });
		
		// now test for each controller in the MEC
		assertEquals(true, list != null);
		assertEquals(true, list.contains(TestMEC.MEC_SUB_ID_1));
		assertEquals(true, list.contains(TestMEC.MEC_SUB_ID_2));
		assertEquals(true, list.contains(TestMEC.MEC_SUB_ID_3));
		
	}
}
