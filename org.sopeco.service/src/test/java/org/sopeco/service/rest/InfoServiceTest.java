package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.configuration.TestConfiguration;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class InfoServiceTest extends JerseyTest {

	public InfoServiceTest() {
		super();
	}
	
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
	
	@Test
	public void testServiceRunning() {
		Boolean b = resource().path(ServiceConfiguration.SVC_INFO)
							  .accept(MediaType.APPLICATION_JSON)
							  .get(Boolean.class);

		assertEquals(true, b);
	}
	
}
