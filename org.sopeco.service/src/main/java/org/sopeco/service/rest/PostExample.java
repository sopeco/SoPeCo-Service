package org.sopeco.service.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;

@Path("example")
public class PostExample {

	@POST
	@Path("test/{test}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Boolean test(@PathParam("test") String test,
							ExperimentSeriesDefinition esd){
		return true;
	}
	
}
