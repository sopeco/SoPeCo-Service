package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.shared.Message;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class AccountServiceTest extends JerseyTest {

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
	public void createExistingAccount() {
		String username = "testuser";
		String password = "testpassword";
		
		// just create the account once to be sure it already exists
		resource().path("account").path("create").path(username).path(password).get(Message.class);
		
		Message m = resource().path("account").path("create").path(username).path(password).get(Message.class);

		assertEquals(true, m.failed());
	}

	/**
	 * Checks it the account with the name already exists (after creating it).
	 */
	@Test
	public void checkExistingAccount() {
		String username = "testuser";
		
		// the creation might fail, but we are only interested if afterwards at least one user
		// with username "testuser" already exists
		resource().path("account").path("create").path(username).path("rand").get(Message.class);

		Boolean b = resource().path("account").path("user")
				.path(username).accept(MediaType.APPLICATION_JSON)
				.get(Boolean.class);

		assertEquals(true, b);
	}
	
}
