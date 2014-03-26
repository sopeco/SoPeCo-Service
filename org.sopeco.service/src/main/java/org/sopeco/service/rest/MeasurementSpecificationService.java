package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.SimpleEntityFactory;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;

/**
 * This class handles the meeasurement specifications (MS). 
 * A {@link ScenarioDefinition} does have a list of {@link MeasurementSpecification}s and a
 * {@link MeasurementEnvironmentDefinition}.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_MEASUREMENT)
public class MeasurementSpecificationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementSpecificationService.class);

	/**
	 * As the scenario definition must be always loaded before the MeasurementSpecification can 
	 * be changed, the variable is often used. It's just a shortener.
	 */
	private static final String NAME = ServiceConfiguration.SVCP_MEASUREMENT_SCENARIONAME;
	
	/**
	 * Lists all the current {@link MeasurementSpecification}s for the user with the given token.
	 * 
	 * @param usertoken 	the user identification
	 * @param scenarioName	the scenario name
	 * @return 				{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 						OK with {@link Entity} List<String> of all {@link MeasurementSpecification}
	 */
	@GET
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_MEASUREMENT_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeasurementSpecificationNames(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
														@PathParam(NAME) String scenarioName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection to account database found.");
			return Response.status(Status.UNAUTHORIZED).build();
		} 
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioName);
			
			if (scenarioDefinition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return Response.status(Status.CONFLICT).entity("ScenarioDefinition is invalid.").build();
			}
			
			List<String> returnList = new ArrayList<String>();
			for (MeasurementSpecification ms : scenarioDefinition.getMeasurementSpecifications()) {
				returnList.add(ms.getName());
			}
			
			return Response.ok(returnList).build();
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Cannot fetch ScenarioDefinition from account database.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot fetch ScenarioDefinition from account database.").build();
		}
		
	}
	
	/**
	 * List all the {@link MeasurementSpecification} as it. So the object returnde is a list of MS objects.
	 * 
	 * @param usertoken		the user identification
	 * @param scenarioName	the scenario name
	 * @return 				{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 						OK with {@link Entity} List<{@link MeasurementSpecification}>
	 */
	@GET
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_MEASUREMENT_LISTSPECS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeasurementSpecifications(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
													@PathParam(NAME) String scenarioName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection to account database found.");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioName);
			
			if (scenarioDefinition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return Response.status(Status.CONFLICT).entity("ScenarioDefinition is invalid.").build();
			}
			
			List<MeasurementSpecification> returnList = new ArrayList<MeasurementSpecification>();
			for (MeasurementSpecification ms : scenarioDefinition.getMeasurementSpecifications()) {
				returnList.add(ms);
			}
			
			return Response.ok(returnList).build();
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Cannot fetch ScenarioDefinition from account database.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot fetch ScenarioDefinition from account database.").build();
		}
		
	}
	
	/**
	 * Switch to a given {@link MeasurementSpecification} (MS). If the MS does not exist, the switch fails.
	 * 
	 * @param usertoken 		the user identification
	 * @param specificationName the name of the MS to switch to
	 * @param scenarioName		the scenario name
	 * @return 					{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	/*@PUT
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public Response switchWorkingSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
										       @QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_SPECNAME) String specificationName,
										       @PathParam(NAME) String scenarioName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		LOGGER.debug("Set working specification to: " + specificationName);

		if (existSpecification(specificationName, scenarioName, usertoken)) {
			LOGGER.debug("Can't set working specification to '{}' because it doesn't exists. ", specificationName);
			return Response.status(Status.CONFLICT).entity("Can't set working specification.").build();
		}
		
		u.setMeasurementSpecification(specificationName);
		
		// update the builder
		MeasurementSpecificationBuilder msb = new MeasurementSpecificationBuilder(u.getCurrentScenarioDefinitionBuilder(), specificationName);
		u.getCurrentScenarioDefinitionBuilder().setMeasurementSpecificationBuilder(msb);
		
		ServicePersistenceProvider.getInstance().storeUser(u);

		// update the AccountDetails#
		ServiceStorageModul.updateAccountDetailsSelectedSpecification(usertoken, specificationName);
		
		return Response.ok().build();
	}*/
	
	/**
	 * Creates a new {@link MeasurementSpecification} (MS).<br />
	 * Return false, if a MS with the given name already exists or the addition failed.
	 * 
	 * @param usertoken 		the user identification
	 * @param specificationName the name for the new {@link MeasurementSpecification}
	 * @return 					{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR
	 */
	@POST
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_MEASUREMENT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
									    @QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_SPECNAME) String specificationName,
									    @PathParam(NAME) String scenarioName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (existSpecification(specificationName, scenarioName, usertoken)) {
			LOGGER.warn("Specification with the name '{}' already exists.", specificationName);
			return Response.status(Status.CONFLICT).entity("Specification with the given name already exists.").build();
		}

		MeasurementSpecification ms = SimpleEntityFactory.createMeasurementSpecification(specificationName);
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		sd.getMeasurementSpecifications().add(ms);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);

		return Response.ok().build();
	}
	
	/**
	 * Renames the given  {@link MeasurementSpecification} (MS) via name with the new given name.
	 * Changes the name of the user's selected MS, too.
	 * 
	 * @param usertoken the user identification
	 * @param specname 	the new MS name
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path("{" + NAME + "}/{" + ServiceConfiguration.SVC_MEASUREMENT_NAME + "}/" + ServiceConfiguration.SVC_MEASUREMENT_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameWorkingSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
											   @QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_SPECNAME) String newMeasurementSpecificationName,
											   @PathParam(NAME) String scenarioName,
											   @PathParam(ServiceConfiguration.SVC_MEASUREMENT_NAME) String measurementSpecificationName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (existSpecification(newMeasurementSpecificationName, scenarioName, usertoken)) {
			LOGGER.warn("Can't rename, because specification with the name '{}' already exists.", newMeasurementSpecificationName);
			return Response.status(Status.CONFLICT).entity("Can't rename, because specification with the given name already exists.").build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		MeasurementSpecification ms = sd.getMeasurementSpecification(measurementSpecificationName);
		
		if (ms == null) {
			LOGGER.warn("No MeasurementSpecification with the name '{}' exists.", measurementSpecificationName);
			return Response.status(Status.CONFLICT).entity("No MeasurementSpecification with the given name exists.").build();	
		}
		
		ms.setName(newMeasurementSpecificationName);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Removes the {@link MeasurementSpecification} with the given name.
	 * 
	 * @param usertoken the user identification
	 * @param specname 	the new MS name
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					CONFLICT can occur, when the {@link MeasurementSpecification} with the passed
	 * 					name has been selected
	 */
	@DELETE
	@Path("{" + NAME + "}/{" + ServiceConfiguration.SVC_MEASUREMENT_NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeWorkingSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
											   @PathParam(NAME) String scenarioName,
											   @PathParam(ServiceConfiguration.SVC_MEASUREMENT_NAME) String measurementSpecificationName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (!existSpecification(measurementSpecificationName, scenarioName, usertoken)) {
			LOGGER.warn("Can't delete MeasurementSpecification, because it doesn't exist.");
			return Response.status(Status.CONFLICT).entity("Can't delete MeasurementSpecification, because it doesn't exist.").build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		MeasurementSpecification ms = sd.getMeasurementSpecification(measurementSpecificationName);
		
		if (ms == null) {
			LOGGER.warn("No MeasurementSpecification with the name '{}' exists.", measurementSpecificationName);
			return Response.status(Status.CONFLICT).entity("No MeasurementSpecification with the given name exists.").build();	
		}
		
		sd.getMeasurementSpecifications().remove(ms);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);

		return Response.ok().build();
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// HELPER /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns whether a {@link MeasurementSpecification} with the given name exists.
	 * 
	 * @param specification MS name
	 * @param scenarioName	the name of the scenario
	 * @param token			the user identification
	 * @return 				true, if MS with the given name exists
	 */
	private boolean existSpecification(String specification, String scenarioName, String token) {

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(token);
		
		ScenarioDefinition sd = null;
		
		try {
			
			sd = dbCon.loadScenarioDefinition(scenarioName);
			
		} catch (DataNotFoundException e) {
			
			LOGGER.info("Cannot find a ScenarioDefition with the given name in the database.");
			
		}
		
		if (sd != null) {
		
			for (MeasurementSpecification ms : sd.getMeasurementSpecifications()) {
				
				if (specification.equals(ms.getName())) {
					return true;
				}
				
			}
		
		}
		
		return false;
	}
	
}
