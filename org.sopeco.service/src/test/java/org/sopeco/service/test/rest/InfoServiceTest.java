package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectMapper;
import org.sopeco.service.test.configuration.TestConfiguration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

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
	 * Tests the default service request interface, if the service
	 * is running.
	 */
	@Test
	public void testServiceRunning() {
		Response r = target().path(ServiceConfiguration.SVC_INFO)
							 .path(ServiceConfiguration.SVC_INFO_RUNNING)
							 .request(MediaType.APPLICATION_JSON)
							 .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
	}
	
}
