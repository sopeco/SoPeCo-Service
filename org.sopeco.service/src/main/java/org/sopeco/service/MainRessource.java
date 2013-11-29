package org.sopeco.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/login")
public class MainRessource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response loginRequest() {
		String output = "6A1337B7";
		return Response.status(200).entity(output).build();
 
	}
	
}
