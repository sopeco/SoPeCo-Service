package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.shared.LoginData;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class LoginTest extends JerseyTest {

	public LoginTest() {
		super();
    }
	
	/**
	 * Configure is called on the object creation of a JerseyTest.
	 * It's used to configure where the JerseyTest can find JSON,
	 * the REST service to test and the JSON POJO.
	 * 
	 * @return the configuration
	 */
	@Override
    public WebAppDescriptor configure() {
        return new WebAppDescriptor.Builder(
        		new String []{TestConfiguration.PACKAGE_NAME_JSON, TestConfiguration.PACKAGE_NAME_REST})
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
	
    @Test
    public void loginTest() throws IllegalArgumentException, IOException {
    	LoginData m = resource().path("login").get(LoginData.class);
        
        assertEquals("6A1337B7", m.getAccessToken());
        assertEquals(true, m.getStatus());
    }
}
