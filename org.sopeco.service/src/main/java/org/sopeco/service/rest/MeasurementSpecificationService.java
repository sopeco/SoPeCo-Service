package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.builder.MeasurementSpecificationBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.shared.ServiceResponse;

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
	 * Lists all the current MS for the user with the given token.
	 * 
	 * @param usertoken the user identification
	 * @return 			list of all MS (as names)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<List<String>> getAllSpecificationNames(@QueryParam("token") String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<List<String>>(Status.UNAUTHORIZED, null);
		}
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection to account database found.");
			return new ServiceResponse<List<String>>(Status.INTERNAL_SERVER_ERROR, null);
		} 
		
		String scenarioname = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName();
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioname);
			
			if (scenarioDefinition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return new ServiceResponse<List<String>>(Status.CONFLICT, null, "ScenarioDefinition is invalid.");
			}
			
			List<String> returnList = new ArrayList<String>();
			for (MeasurementSpecification ms : scenarioDefinition.getMeasurementSpecifications()) {
				returnList.add(ms.getName());
			}
			
			return new ServiceResponse<List<String>>(Status.OK, returnList);
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Cannot fetch ScenarioDefinition from account database.");
			return new ServiceResponse<List<String>>(Status.INTERNAL_SERVER_ERROR, null, "Cannot fetch ScenarioDefinition from account database.");
		}
		
	}
	
	/**
	 * List all the MS as it. So the object returnde is a list of MS objects.
	 * 
	 * @param usertoken the user identification
	 * @return 			list of all MS (as objects)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LISTSPECS)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<List<MeasurementSpecification>> getAllSpecifications(@QueryParam("token") String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<List<MeasurementSpecification>>(Status.UNAUTHORIZED, null);
		}
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection to account database found.");
			return new ServiceResponse<List<MeasurementSpecification>>(Status.INTERNAL_SERVER_ERROR, null);
		} 
		
		String scenarioname = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName();
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioname);
			
			if (scenarioDefinition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return new ServiceResponse<List<MeasurementSpecification>>(Status.CONFLICT, null, "ScenarioDefinition is invalid.");
			}
			
			List<MeasurementSpecification> returnList = new ArrayList<MeasurementSpecification>();
			for (MeasurementSpecification ms : scenarioDefinition.getMeasurementSpecifications()) {
				returnList.add(ms);
			}
			return new ServiceResponse<List<MeasurementSpecification>>(Status.OK, returnList);
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Cannot fetch ScenarioDefinition from account database.");
			return new ServiceResponse<List<MeasurementSpecification>>(Status.INTERNAL_SERVER_ERROR, null, "Fetching ScenarioDefinition failed!");
		}
		
	}
	
	/**
	 * Switch to a given MS. If the MS does not exist, the switch fails.
	 * 
	 * @param usertoken 		the user identification
	 * @param specificationName the name of the MS to switch to
	 * @return 					true, if MS exist and was switched to
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> setWorkingSpecification(@QueryParam("token") String usertoken,
										   @QueryParam("specname") String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		LOGGER.debug("Set working specification on: " + specificationName);

		if (!existSpecification(specificationName, u)) {
			LOGGER.debug("Can't set working specification to '{}' because it doesn't exists. ", specificationName);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false);
		}
		
		u.setMeasurementSpecification(specificationName);
		
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Creates a new MS. However, this request does not switch the MS! The MS must be siwtched manually
	 * via the service at @Code{SVC_MEASUREMENT_SWITCH} service.
	 * Return false, if a MS with the given name already exists or the addition failed.
	 * 
	 * @param usertoken 		the user identification
	 * @param specificationName the name for the new MS
	 * @return 					true, if adding was successful and there was no other MS with the given name
	 */
	@POST
	@Path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> createSpecification(@QueryParam("token") String usertoken,
									   @QueryParam("specname") String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		if (existSpecification(specificationName, u)) {
			LOGGER.warn("Specification with the name '{}' already exists.", specificationName);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "Specification with the given name already exists.");
		}

		MeasurementSpecificationBuilder msb = u.getCurrentScenarioDefinitionBuilder()
													  .getNewMeasurementSpecification();
		if (msb == null) {
			LOGGER.warn("Error creating new specification.");
			return new ServiceResponse<Boolean>(Status.INTERNAL_SERVER_ERROR, false, "Error creating new specification.");
		}

		msb.setName(specificationName);
		
		// store the scenario definition in the databse of the current user
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return new ServiceResponse<Boolean>(Status.INTERNAL_SERVER_ERROR, false, "No database connection found.");
		} else {
			ScenarioDefinition scenarioDef = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
			dbCon.store(scenarioDef);
		}
		
		// save the selected specification in the service-db
		ServicePersistenceProvider.getInstance().storeUser(u);

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Renames the current selected MS. Fails if the user has not MS currently selected.
	 * Changes the selected MS of the user to the new name, too.
	 * 
	 * @param usertoken the user identification
	 * @param specname 	the new MS name
	 * @return 			true, if the current selected MS can be renamed. False, if the user has no
	 * 		   			MS currently selected.
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> renameWorkingSpecification(@QueryParam("token") String usertoken,
											  @QueryParam("specname") String specname) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		if (existSpecification(specname, u)) {
			LOGGER.warn("Can't rename, because specification with the name '{}' already exists.", specname);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "Can't rename, because specification with the given name already exists");
		}
		
		MeasurementSpecification msss = u.getCurrentScenarioDefinitionBuilder().getMeasurementSpecification(u.getMeasurementSpecification());
		
		if (msss == null) {
			LOGGER.warn("User has no MeasurementSpecification selected. Therefore a renaming cannot be completed.");
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "No MeasurementSpecification selected yet.");
		}

		msss.setName(specname);
		
		// the user has now another selected MeasurementSpecification
		u.setMeasurementSpecification(specname);
		
		// store the scenario definition in the databse of the current user
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found for the user with the token '{}'.", usertoken);
			return new ServiceResponse<Boolean>(Status.INTERNAL_SERVER_ERROR, false, "No database connection found.");
		}
		
		// store the scenario definiton with the new measurementspecification in the database
		ScenarioDefinition scenarioDef = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		dbCon.store(scenarioDef);

		// store the new user data in it's database
		ServicePersistenceProvider.getInstance().storeUser(u);

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	
	
	// *************************** (private) HELPER *****************************************
	
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
}
