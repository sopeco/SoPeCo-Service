package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.exchange.ServiceResponse;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * The <code>InfoServiceTest</code> tests various features of the
 * <code>InfoService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class InfoServiceTest extends JerseyTest {

	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
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
	
	/**
	 * Tests the default service request interface, if the service
	 * is running.
	 */
	@Test
	public void testServiceRunning() {
		ServiceResponse<Boolean> sr_b = resource().path(ServiceConfiguration.SVC_INFO)
												  .path(ServiceConfiguration.SVC_INFO_RUNNING)
												  .accept(MediaType.APPLICATION_JSON)
												  .get(new GenericType<ServiceResponse<Boolean>>() { });

		assertEquals(true, sr_b.getObject());
	}
	
}
