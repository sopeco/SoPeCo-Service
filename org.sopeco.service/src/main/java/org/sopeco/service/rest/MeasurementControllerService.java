package org.sopeco.service.rest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.IMeasurementEnvironmentController;
import org.sopeco.engine.measurementenvironment.connector.MEConnectorFactory;
import org.sopeco.engine.measurementenvironment.socket.SocketAppWrapper;
import org.sopeco.engine.measurementenvironment.socket.SocketManager;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.ServiceStorageModul;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.exchange.MECStatus;

/**
 * The <code>MeasurementControllerService</code> provides services to handle MeasurementEnvironemtControllers (MEC)
 * of SoPeCo. The service alowes to request information about connected MECs.<br />
 * Currently only socket connection are allowed and can be used.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_MEC)
public class MeasurementControllerService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementControllerService.class);
	
	/**
	 * The URL pattern for a controller URL.
	 */
	private static final String[] CONTROLLER_URL_PATTERN = new String[] { "^socket://[a-zA-Z0-9\\.]+(:[0-9]{1,5})?/[a-zA-Z][a-zA-Z0-9]*$" };
	
	/**
	 * Returns the status of the given host and port tupel. Tries to connect with a
	 * {@link Socket}.
	 * 
	 * @return {@link Response} OK or INTERNAL_SERVER_ERROR
	 * 						    
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEC_PORTREACHABLE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response isPortReachable(@QueryParam(ServiceConfiguration.SVCP_MEC_HOST) String host,
									@QueryParam(ServiceConfiguration.SVCP_MEC_PORT) int port) {
		
		try {
			
			SocketAddress socketAddress = new InetSocketAddress(host, port);
			Socket socket = new Socket();
			socket.connect(socketAddress, ServiceConfiguration.SOCKET_TIMEOUT);
			socket.close();
			return Response.ok().build();
			
		} catch (UnknownHostException e) {
			LOGGER.info("Unknown host '" + host + ":" + port + "'");
		} catch (IOException e) {
			LOGGER.info("IOException at connection to '" + host + ":" + port + "'");
		}
		
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Returns the URI pattern for the socket connection URL.
	 * 
	 * @return {@link Response} with the valid URL pattern as {@link Entity}the URI pattern for the socket connection URL
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEC_VALIDATE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getValidUrlPattern() {
		return Response.ok(CONTROLLER_URL_PATTERN).build();
	}
	
	/**
	 * Returns the status of the MEC on the given URL. The status of the MEC can be fetched via
	 * the {@link MECStatus}. If the URL is inavlid or something happened to the MEC,
	 * <code>null</code> might be returned.
	 * 
	 * @param usertoken authentification of the user
	 * @param url 		the URL to the MEC
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					OK with {@link MECStatus} as {@link Entity}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEC_STATUS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMECStatus(@QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken,
								 @QueryParam(ServiceConfiguration.SVCP_MEC_URL) String url) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		if (!checkUrlIsValid(url) || url == null) {
			LOGGER.debug("Controller-Status: NO_VALID_MEC_URL");
			return Response.ok(new MECStatus(MECStatus.NO_VALID_MEC_URL)).build();
		}
		
	
		try {
			
			IMeasurementEnvironmentController mec = MEConnectorFactory.connectTo(new URI(url));

			if (mec == null) {
				LOGGER.debug("Controller not reachable: NO_VALID_MEC_URL");
				return Response.ok(new MECStatus(MECStatus.NO_VALID_MEC_URL)).build();
			}
			
			MeasurementEnvironmentDefinition med = mec.getMEDefinition();

			if (med == null) {
				LOGGER.debug("Controller-Status: STATUS_ONLINE_NO_META");
				return Response.ok(new MECStatus(MECStatus.STATUS_ONLINE_NO_META)).build();
			} else {
				LOGGER.debug("Controller-Status: STATUS_ONLINE");
				return Response.ok(new MECStatus(MECStatus.STATUS_ONLINE)).build();
			}

		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
			return Response.status(Status.CONFLICT).entity("Invalid URI.").build();
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Connection establishing failed.").build();
		} catch (IllegalStateException x) {
			LOGGER.debug("Controller-Status: STATUS_OFFLINE");
			return Response.ok(new MECStatus(MECStatus.STATUS_OFFLINE)).build();
		}
	
	}
	
	/**
	 * This methods returns the whole list for connected controllers on the given MEC ID.
	 * 
	 * @param usertoken authentification of the user
	 * @param id 		the MEC ID
	 * @return 			{@link Response} OK, UNAUTHORIZED or CONFLICT
	 * 					OK with {@link Entity} List<String> of all controllers currently
	 * 					connected to given MEC ID with given<br />
	 * 					CONFLICT indicates, that no MEC with given ID is connected
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEC_LIST)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getControllerList(@QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken,
			      				      @QueryParam(ServiceConfiguration.SVCP_MEC_ID) String id) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		SocketAppWrapper app = SocketManager.getSocketApp(id);
		
		if (app == null) {
			LOGGER.info("SocketAppWrapper is in invalid state.");
			return Response.status(Status.CONFLICT).entity("No MEC with given ID connected.").build();
		}
			
		List<String> tmpList = Arrays.asList(app.getAvailableController());

		return Response.ok(tmpList).build();
	}
	
	/**
	 * Before: The MeasurementEnvironmentController should be connected to the service
	 * ServerSocket. Otherwise the method is going to fail and return null.<br />
	 * <br />
	 * The MED is requested from the from the MEC connected to the given URL.
	 * The MED is returned WITHOUT being stored in the database. This has to be done
	 * manually via the service at med/set!
	 * 
	 * @param usertoken authentification of the user
	 * @param uri 		the URI of the MeasurementEnvironmentController already connected to the
	 * 		  			ServerSocket of the service
	 * @return 			{@link Response} OK, UNAUTHORIZED, ACCEPTED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					OK with the {@link MeasurementEnvironmentDefinition} to the MEC on the given URI as {@link Entity}<br />
	 * 					ACCEPTED indicates that the information from the MEC could not be fetched
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEC_MED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMEDefinitionFromMEC(@QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken,
									 	   @QueryParam(ServiceConfiguration.SVCP_MEC_URL) String uri) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			// connect to the mec via Socket
			IMeasurementEnvironmentController mec = MEConnectorFactory.connectTo(new URI(uri));
			
			if (mec == null) {
				LOGGER.info("The connected MEC cannot be fetched correctly.");
				return Response.status(Status.ACCEPTED).entity("The connected MEC cannot be fetched correctly.").build();
			}
			
			MeasurementEnvironmentDefinition med = mec.getMEDefinition();
			
			if (med == null) {
				LOGGER.info("The connected med has no valid MeasurementEnvironmentDefinition.");
				return Response.status(Status.CONFLICT).entity("The connected med has no valid MeasurementEnvironmentDefinition.").build();
			}
			
			return Response.ok(med).build();
			
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
			return Response.status(Status.CONFLICT).entity("URI invalid.").build();
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Remote exception.").build();
		} catch (IllegalStateException x) {
			LOGGER.error("Controller probably offline.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Controller probably offline.").build();
		}
	}
	
	
	/**
	 * Before: The MeasurementEnvironmentController (MEC) should be connected to the service
	 * ServerSocket. Otherwise the method is going to fail and return false.<br />
	 * After getting the {@link MeasurementEnvironmentDefinition} (MED) from the MEC, the MED
	 * is stored in the {@link ScenarioDefinition} - given via it's name - the account database.<br />
	 * This method only uses the services at {@link #getMEDefinitionFromMEC(String, String)}.
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken 	authentification of the user
	 * @param uri 			the URI of the MeasurementEnvironmentController already connected to the
	 * 		  				ServerSocket of the service
	 * @return 				{@link Response} OK, UNAUTHORIZED, ACCEPTED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 						ACCEPTED indicates that the information from the MEC could not be fetched
	 */
	@POST
	@Path("{" + ServiceConfiguration.SVC_MEC_SCENARIONAME + "}/" + ServiceConfiguration.SVC_MEC_MED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setMEDefinitionFromMEC(@PathParam(ServiceConfiguration.SVC_MEC_SCENARIONAME) String scenarioName,
										   @QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken,
										   @QueryParam(ServiceConfiguration.SVCP_MEC_URL) String uri) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		Response r = getMEDefinitionFromMEC(usertoken, uri);
		
		if (r.getStatus() == Status.OK.getStatusCode()) {

			MeasurementEnvironmentDefinition med = r.readEntity(MeasurementEnvironmentDefinition.class);
			
			ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
			
			if (sd == null) {
				LOGGER.info("No ScenarioDefinition with given name.", usertoken);
				return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
			}
			
			sd.setMeasurementEnvironmentDefinition(med);
			
			if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store MED in database.").build();
			}
			
			return Response.ok().build();
		}
		
		return r;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// HELPER /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Checks if the given URI is like a valid pattern.
	 * 
	 * @param uri 	the URI to check
	 * @return 		true, if the URI has a valid pattern
	 */
	private boolean checkUrlIsValid(String uri) {
		
		for (String pattern : CONTROLLER_URL_PATTERN) {
			if (uri.matches(pattern)) {
				return true;
			}
		}

		return false;
	}
	
}
