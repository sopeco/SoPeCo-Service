package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.configuration.ServletContainerLifecycleListener;
import org.sopeco.service.rest.MeasurementControllerService;
import org.sopeco.service.rest.exchange.MECStatus;
import org.sopeco.service.test.configuration.TestConfiguration;
import org.sopeco.service.test.rest.fake.TestMEC;

/**
 * The {@link MeasurementControllerServiceTest} tests the {@link MeasurementControllerService} class.
 * The {@link ServletContextListener} of class {@link ServletContainerLifecycleListener} is connected to the test container
 * and starts the ServerSocket.
 * <br />
 * <br />
 * The ServerSocket is started on each test, because the testcontainer is always shut down. This is not
 * a bug, but a feature in JerseyTest. A ticket has been opened with the issue
 * <code>https://java.net/jira/browse/JERSEY-705</code>.
 * 
 * @author Peter Merkert
 */
public class MeasurementControllerServiceTest extends AbstractServiceTest {

	private static Logger LOGGER = LoggerFactory.getLogger(MeasurementControllerServiceTest.class);
	
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
		
		String token = login(accountname, password);
		
		// connect to a random string
		Response r = target().path(ServiceConfiguration.SVC_MEC)
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
		
		String token = login(accountname, password);

		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		// now the controller status is requested
		Response r = target().path(ServiceConfiguration.SVC_MEC)
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

		String token = login(accountname, password);

		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		// now the controller status is requested
		Response r = target().path(ServiceConfiguration.SVC_MEC)
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
