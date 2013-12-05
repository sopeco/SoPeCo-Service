package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.sopeco.service.configuration.TestConfiguration;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class AccountServiceTest extends JerseyTest {

	public AccountServiceTest() {
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
    public void createExistingAccount() throws IllegalArgumentException, IOException {
    	String username = "test";
    	String password = "test";

        WebResource webResource = resource().path("account").path("create").path(username).path(password);
        ClientResponse client = webResource.head();
        
        assertEquals(403, client.getStatus());
        
        //assertEquals("{\"message\":\"Account with the name test already exists\"}", webResource.accept(APPLICATION_JSON).get(String.class));
    }
	
}
