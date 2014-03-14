package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.builder.MeasurementSpecificationBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.ScenarioDetails;
import org.sopeco.service.persistence.entities.Users;

/**
 * This class handles the meeasurement specifications (MS). 
 * A ScenarioDefinition does have a list of MeasurementSpecifications and a
 * MeasurementEnviromentDefinition.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_MEASUREMENT)
public class MeasurementSpecificationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementSpecificationService.class);
	
	/**
	 * Lists all the current {@link MeasurementSpecification}s for the user with the given token.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					OK with {@link Entity} List<String> of all {@link MeasurementSpecification}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSpecificationNames(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken) {
		
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
		
		String scenarioname = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName();
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioname);
			
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
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					OK with {@link Entity} List<{@link MeasurementSpecification}>
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LISTSPECS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSpecifications(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken) {

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
		
		String scenarioname = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName();
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioname);
			
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
	 * @return 					{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setWorkingSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
										    @QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_SPECNAME) String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		LOGGER.debug("Set working specification on: " + specificationName);

		if (!existSpecification(specificationName, u)) {
			LOGGER.debug("Can't set working specification to '{}' because it doesn't exists. ", specificationName);
			return Response.status(Status.CONFLICT).entity("Can't set working specification.").build();
		}
		
		u.setMeasurementSpecification(specificationName);
		
		ServicePersistenceProvider.getInstance().storeUser(u);

		// update the AccountDetails
		updateAccountDetails(usertoken, specificationName);
		
		return Response.ok().build();
	}
	
	/**
	 * Creates a new {@link MeasurementSpecification} (MS). However, this request does not switch the MS
	 * The MS must be siwtched manually via the service at @Code{SVC_MEASUREMENT_SWITCH} service.
	 * Return false, if a MS with the given name already exists or the addition failed.
	 * 
	 * @param usertoken 		the user identification
	 * @param specificationName the name for the new MS
	 * @return 					{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR
	 */
	@POST
	@Path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
									    @QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_SPECNAME) String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (existSpecification(specificationName, u)) {
			LOGGER.warn("Specification with the name '{}' already exists.", specificationName);
			return Response.status(Status.CONFLICT).entity("Specification with the given name already exists.").build();
		}

		MeasurementSpecificationBuilder msb = u.getCurrentScenarioDefinitionBuilder()
													  .getNewMeasurementSpecification();
		if (msb == null) {
			LOGGER.warn("Error creating new specification.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error creating new specification.").build();
		}

		msb.setName(specificationName);
		
		// store the scenario definition in the databse of the current user
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("No database connection found.").build();
		} else {
			ScenarioDefinition scenarioDef = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
			dbCon.store(scenarioDef);
		}
		
		// save the selected specification in the service-db
		ServicePersistenceProvider.getInstance().storeUser(u);

		return Response.ok().build();
	}
	
	/**
	 * Renames the current selected MS. Fails if the user has not MS currently selected.
	 * Changes the selected MS of the user to the new name, too.
	 * 
	 * @param usertoken the user identification
	 * @param specname 	the new MS name
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					CONFLICT can occur, when no {@link MeasurementSpecification} has been selected yet
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameWorkingSpecification(@QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_TOKEN) String usertoken,
											   @QueryParam(ServiceConfiguration.SVCP_MEASUREMENT_SPECNAME) String specname) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (existSpecification(specname, u)) {
			LOGGER.warn("Can't rename, because specification with the name '{}' already exists.", specname);
			return Response.status(Status.CONFLICT).entity("Can't rename, because specification with the given name already exists.").build();
		}
		
		MeasurementSpecification msss = u.getCurrentScenarioDefinitionBuilder().getMeasurementSpecification(u.getMeasurementSpecification());
		
		if (msss == null) {
			LOGGER.warn("User has no MeasurementSpecification selected. Therefore a renaming cannot be completed.");
			return Response.status(Status.CONFLICT).entity("No MeasurementSpecification selected yet.").build();
		}
		
		msss.setName(specname);
		
		// the user has now another selected MeasurementSpecification
		u.setMeasurementSpecification(specname);
		
		// store the scenario definition in the databse of the current user
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found for the user with the token '{}'.", usertoken);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("No database connection found.").build();
		}
		
		// store the scenario definiton with the new measurementspecification in the database
		ScenarioDefinition scenarioDef = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		dbCon.store(scenarioDef);

		// store the new user data in it's database
		ServicePersistenceProvider.getInstance().storeUser(u);

		// update the AccountDetails
		updateAccountDetails(usertoken, specname);

		return Response.ok().build();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// HELPER /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns whether a MS with the given name exists.
	 * 
	 * @param specification MS name
	 * @return 				true, if MS with the given name exists
	 */
	private boolean existSpecification(String specification, Users u) {

		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder()
										    .getScenarioDefinition()
										    .getMeasurementSpecifications()) {
			if (specification.equals(ms.getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Update the {@link AccountDetails} for the account connected to the {@link Users} with the given
	 * token.<br />
	 * Check: Before the account must have selected a scenario!<br />
	 * Afterwards the {@link AccountDetails} will be stored in the database.<br />
	 * <br />
	 * Only error outputs are created, because here must be no error!
	 * 
	 * @param usertoken				the token to identify the user
	 * @param specificationName		the name of the selected {@link MeasurementSpecification}
	 */
	private void updateAccountDetails(String usertoken, String specificationname) {

		LOGGER.debug("Trying to update the AccountDetails with a MeasurementSpecification name.");
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.error("The given token is invalid.");
			return;
		}

		AccountDetails ad = u.getAccountDetails();
		
		if (ad == null) {
			LOGGER.error("The user should have selected a scenario before setting the MeasurementSpecification!");
			return;
		}

		String scenarioname = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName();
		
		ScenarioDetails sd = ad.getScenarioDetail(scenarioname);
		
		if (sd == null) {
			LOGGER.error("There must be already a ScenarioDetails object in the AccountDetails list with the given scenario name!");
			return;
		}
		
		sd.setSelectedSpecification(specificationname);
		
		ServicePersistenceProvider.getInstance().storeAccountDetails(ad);
	}
	
}
