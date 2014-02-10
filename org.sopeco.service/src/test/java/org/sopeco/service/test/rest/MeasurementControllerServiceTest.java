package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.StartUpService;
import org.sopeco.service.rest.json.CustomObjectWrapper;
import org.sopeco.service.shared.MECStatus;
import org.sopeco.service.shared.Message;
import org.sopeco.service.test.configuration.TestConfiguration;
import org.sopeco.service.test.rest.fake.TestMEC;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

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
	 * Configure is called on the object creation of a JerseyTest. It's used to
	 * configure where the JerseyTest can find JSON, the REST service to test
	 * and the JSON POJO.
	 * 
	 * @return the configuration
	 */
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(TestConfiguration.PACKAGE_NAME_REST)
				.contextListenerClass(StartUpService.class)
				.clientConfig(createClientConfig())
				.build();
	}

	/**
	 * Sets the client config for the client. The method adds a special {@link CustomObjectWrapper}
	 * to the normal Jackson wrapper for JSON.
	 * This method is called by {@link configure()}.
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new DefaultClientConfig();
	    // the class contains the configuration to ignore not mappable properties
	    config.getClasses().add(CustomObjectWrapper.class);
	    return config;
	}
	
	/**
	 * Tests the Status of a MEC. This test only tests and invalid status
	 * and not if the status of a registered MEC is returned correctly.
	 * 
	 * 1. log in
	 * 2. get MEC status (invalid URL)
	 * 3. get MEC status (invalid token)
	 */
	@Ignore
	@Test
	public void testMECStatus() {
		LOGGER.debug("Testing fetch of MEC status.");
		
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// connect to a random string
		MECStatus mecStatus = resource().path(ServiceConfiguration.SVC_MEC)
									    .path(ServiceConfiguration.SVC_MEC_STATUS)
									    .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
									    .queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
									    .get(MECStatus.class);
		
		assertEquals(MECStatus.NO_VALID_MEC_URL, mecStatus.getStatus());
	
		// check if a wrong token fails, too
		mecStatus = resource().path(ServiceConfiguration.SVC_MEC)
						      .path(ServiceConfiguration.SVC_MEC_STATUS)
						      .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, "myrandomtoken")
						      .queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
						      .get(MECStatus.class);
		
		assertEquals(-1, mecStatus.getStatus());
		
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
	@Ignore
	@Test
	public void testMECStatusValidController() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		String socketURI 	= "socket://" + TestMEC.MEC_ID + "/" + TestMEC.MEC_SUB_ID_1;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();

		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		// now the controller status is requested
		MECStatus mecStatus = resource().path(ServiceConfiguration.SVC_MEC)
								        .path(ServiceConfiguration.SVC_MEC_STATUS)
								        .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
								        .queryParam(ServiceConfiguration.SVCP_MEC_URL, socketURI)
								        .get(MECStatus.class);
		
		assertEquals(MECStatus.STATUS_ONLINE, mecStatus.getStatus());
		
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
	@Ignore
	@Test
	public void testMECGetControllerList() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		String mecID 		= TestMEC.MEC_ID;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();

		// now start the MEC fake, which connects to the ServerSocket created by the RESTful service
		TestMEC.start();
		
		// now the controller status is requested
		List<String> controllerList = resource().path(ServiceConfiguration.SVC_MEC)
										        .path(ServiceConfiguration.SVC_MEC_LIST)
										        .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
										        .queryParam(ServiceConfiguration.SVCP_MEC_ID, mecID)
										        .get(new GenericType<List<String>>() { });
		
		// now test for each controller in the MEC
		assertEquals(true, controllerList.contains(TestMEC.MEC_SUB_ID_1));
		assertEquals(true, controllerList.contains(TestMEC.MEC_SUB_ID_2));
		assertEquals(true, controllerList.contains(TestMEC.MEC_SUB_ID_3));
		
	}
}
