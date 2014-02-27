package org.sopeco.service.configuration;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.sopeco.service.rest.json.CustomObjectMapper;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * This class is registered as an {@link Application} class at the Jersey web.xml settings
 * and is called once the servlet is started up. The class is needed to register the custom
 * {@link JacksonJaxbJsonProvider} to the RESTful service, to get the {@link CustomObjectMapper}. It's also
 * needed to register the {@link ServletContainerLifecycleListener} to listen for changes
 * in the servlet context.
 * <br />
 * <br />
 * For more information, please visit
 * <a href="https://jersey.java.net/documentation/latest/deployment.html#d0e2747">Jersey SPI ResourceConfig
 * registration</a>.
 * 
 * @author Peter Merkert
 */
public class ServletContainerInitialization extends ResourceConfig {
	
	/**
	 * The constructor is used to register the {@link CustomObjectMapper} in the {@link Application}. And
	 * it's used to register the {@link ServletContainerLifecycleListener}.
	 */
    public ServletContainerInitialization() {
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());
		register(provider);
		
		packages(ServiceConfiguration.PACKAGE_NAME_LIFECYCLELISTENER);
    }
}
