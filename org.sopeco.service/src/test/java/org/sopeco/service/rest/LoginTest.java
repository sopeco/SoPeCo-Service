package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.sopeco.service.configuration.TestConfiguration;

import com.sun.jersey.api.client.WebResource;
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
				.build();
    }
	
    @Test
    public void loginTest() throws IllegalArgumentException, IOException {
        WebResource webResource = resource().path("login");
        assertEquals("{\"accessToken\":\"6A1337B7\"}", webResource.accept(MediaType.APPLICATION_JSON).get(String.class));
    }
}
