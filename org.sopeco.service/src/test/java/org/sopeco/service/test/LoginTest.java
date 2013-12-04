package org.sopeco.service.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class LoginTest extends JerseyTest {
	private static WebAppDescriptor appDescriptor;
	static{
		appDescriptor= new WebAppDescriptor.Builder(new String []{"org.codehaus.jackson.jaxrs","org.sopeco.service.login"})
		.initParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
//		.initParam("jersey.config.server.provider.packages", "org.codehaus.jackson.jaxrs;org.sopeco.service.login")
		.build();
	}
	private static final String PACKAGE_NAME = "org.sopeco.service.login;org.codehaus.jackson.jaxrs";
	
	public LoginTest() {
		super(appDescriptor);
    }
	
	
//	@Override
//    public WebAppDescriptor configure() {
//        return new WebAppDescriptor.Builder()
//            	.initParam(WebComponent.RESOURCE_CONFIG_CLASS,
//                      ClassNamesResourceConfig.class.getName())
//                .initParam(
//                      ClassNamesResourceConfig.PROPERTY_CLASSNAMES,
//                      TodoResource.class.getName() + ";"
//                              + MockTodoServiceProvider.class.getName() + ";"
//                              + NotFoundMapper.class.getName()).build();
//    }
	
    @Test
    public void test() throws IllegalArgumentException, IOException {
    	
        WebResource webResource = resource().path("login");
        
        assertEquals("{\"accessToken\":\"6A1337B7\"}", webResource.accept(MediaType.APPLICATION_JSON).get(String.class));
    }
	
}
