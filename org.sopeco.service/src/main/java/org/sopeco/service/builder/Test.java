package org.sopeco.service.builder;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class Test {
	
public static void main(String[] args) {
	
	Client client = Client.create(createClientConfig());
	WebResource resource = client.resource("http://localhost:8080");
	
	ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
	boolean bo = resource.path("example")
	  .path("test")
	  .path("test")
	  .accept(MediaType.APPLICATION_JSON)
	  .type(MediaType.APPLICATION_JSON)
	  .post(Boolean.class, esd);
}

private static ClientConfig createClientConfig() {
	ClientConfig config = new DefaultClientConfig();
    config.getClasses().add(JacksonJsonProvider.class);
    config.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE);
    
    return config;
}
}
