package org.sopeco.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/login")
public class MainRessource {

	@GET
	@Produces("text/plain")
	public Response loginRequest() {
 
		String output = "6A1337B7";
		return Response.status(200).entity(output).build();
 
	}
	
  @GET
  @Produces(MediaType.TEXT_XML)
  public String sayXMLHello() {
	  
    return "<?xml version=\"1.0\"?>" + "<hello> Hello Jersey" + "</hello>";
    
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String sayHtmlHello() {
    return "<html> " + "<title>" + "Hello Jersey" + "</title>" + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
  }
	
}
