package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.shared.ServiceResponse;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * The <code>AccountServiceTest</code> tests various features of the
 * <code>AccountService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class AccountServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceTest.class.getName());
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public AccountServiceTest() {
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
	 * Checks if it is possible to resgister an account twice.
	 */
	@Test
	public void testAccountDoubleCreation() {
		LOGGER.debug("Starting test for double account creation.");
		
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		// just create the account once to be sure it already exists
		resource().path(ServiceConfiguration.SVC_ACCOUNT)
				  .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_PASSWORD, password)
				  .post(new GenericType<ServiceResponse<Boolean>>() { });
		
		ServiceResponse<Boolean> sr = resource().path(ServiceConfiguration.SVC_ACCOUNT)
										        .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
										        .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
										        .queryParam(ServiceConfiguration.SVC_ACCOUNT_PASSWORD, password)
										        .post(new GenericType<ServiceResponse<Boolean>>() { });

		assertEquals(false, sr.getObject());
		assertEquals(true, Status.FORBIDDEN == sr.getStatus());
	}

	/**
	 * Checks it the account with the name already exists (after creating it).
	 */
	@Test
	public void testCheckAccountExistence() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		// the creation might fail, but we are only interested if afterwards at least one user
		// with username "testuser" already exists
		resource().path(ServiceConfiguration.SVC_ACCOUNT)
				  .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_PASSWORD, password)
				  .post(new GenericType<ServiceResponse<Boolean>>() { });

		ServiceResponse<Boolean> b = resource().path(ServiceConfiguration.SVC_ACCOUNT)
											   .path(ServiceConfiguration.SVC_ACCOUNT_EXISTS)
											   .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
											   .accept(MediaType.APPLICATION_JSON)
											   .get(new GenericType<ServiceResponse<Boolean>>() { });

		assertEquals(true, b.getObject());
	}
	
}
