package org.sopeco.service.login;

import org.sopeco.service.shared.LoginData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

@Path("login")
@XmlRootElement
public class Login {

	final static String ACCESS_TOKEN = "6A1337B7";
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginRequest() {
		
		LoginData loginData = new LoginData(ACCESS_TOKEN);
		return Response.status(201).entity(loginData).build();
 
	}
	
}
