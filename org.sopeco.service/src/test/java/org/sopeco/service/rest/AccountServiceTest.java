package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.shared.Message;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class AccountServiceTest extends JerseyTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceTest.class.getName());
	
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
		return new WebAppDescriptor.Builder(new String[] {
				TestConfiguration.PACKAGE_NAME_JSON,
				TestConfiguration.PACKAGE_NAME_REST })
				.initParam(TestConfiguration.PACKAGE_NAME_POJO, "true")
				.clientConfig(createClientConfig())
				.build();
	}

	/**
	 * Sets the client config for the client to accept JSON and
	 * converting JSON Object to POJOs.
	 * This method is called by {@link configure()}.
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new DefaultClientConfig();
	    config.getClasses().add(JacksonJsonProvider.class);
	    config.getFeatures().put(TestConfiguration.PACKAGE_NAME_POJO, Boolean.TRUE);
	    return config;
	}
	
	/**
	 * Checks if it is possible to resgister an account twice.
	 */
	@Test
	public void testAccountDoubleCreation() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// just create the account once to be sure it already exists
		resource().path(ServiceConfiguration.SVC_ACCOUNT)
				  .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_PASSWORD, password)
				  .post(Message.class);
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
						      .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
							  .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
							  .queryParam(ServiceConfiguration.SVC_ACCOUNT_PASSWORD, password)
						      .post(Message.class);

		assertEquals(true, m.failed());
	}

	/**
	 * Checks it the account with the name already exists (after creating it).
	 */
	@Test
	public void testCheckAccountExistence() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// the creation might fail, but we are only interested if afterwards at least one user
		// with username "testuser" already exists
		resource().path(ServiceConfiguration.SVC_ACCOUNT)
				  .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
				  .queryParam(ServiceConfiguration.SVC_ACCOUNT_PASSWORD, password)
				  .post(Message.class);

		Boolean b = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_EXISTS)
							  .queryParam(ServiceConfiguration.SVC_ACCOUNT_ACCOUNTNAME, accountname)
							  .accept(MediaType.APPLICATION_JSON)
							  .get(Boolean.class);

		assertEquals(true, b);
	}
	
}
