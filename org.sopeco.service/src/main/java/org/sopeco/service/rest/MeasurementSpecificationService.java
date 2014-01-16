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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.builder.MeasurementSpecificationBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
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
	 * Lists all the current MS for the user with the given token.
	 * 
	 * @param usertoken the user identification
	 * @return list of all MS (as names)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getAllSpecificationNames(@QueryParam("token") String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		List<String> returnList = new ArrayList<String>();
		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder().getBuiltScenario().getMeasurementSpecifications()) {
			returnList.add(ms.getName());
		}

		return returnList;
	}
	
	/**
	 * List all the MS as it. So the object returnde is a list of MS objects.
	 * 
	 * @param usertoken the user identification
	 * @return list of all MS (as objects)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LISTSPECS)
	@Produces(MediaType.APPLICATION_JSON)
	public List<MeasurementSpecification> getAllSpecifications(@QueryParam("token") String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		List<MeasurementSpecification> returnList = new ArrayList<MeasurementSpecification>();
		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder().getBuiltScenario()
				.getMeasurementSpecifications()) {
			returnList.add(ms);
		}

		return returnList;
	}
	
	/**
	 * Switch to a given MS. If the MS does not exist, the switch fails.
	 * 
	 * @param usertoken the user identification
	 * @param specificationName the name of the MS to switch to
	 * @return true, if MS exist and was switched to
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean setWorkingSpecification(@QueryParam("token") String usertoken,
										   @QueryParam("specname") String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}
		
		LOGGER.debug("Set working specification on: " + specificationName);

		if (!existSpecification(specificationName, u)) {
			LOGGER.debug("Can't set working specification to '{}' because it doesn't exists. ", specificationName);
			return false;
		}
		
		u.setMeasurementSpecification(specificationName);
		
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return true;
	}
	
	/**
	 * Creates a new MS. However, this request does not switch the MS! The MS must be siwtched manually
	 * via the service at @Code{SVC_MEASUREMENT_SWITCH} service.
	 * Return false, if a MS with the given name already exists or the addition failed.
	 * 
	 * @param usertoken the user identification
	 * @param specificationName the name for the new MS
	 * @return true, if adding was successful and there was no other MS with the given name
	 */
	@POST
	@Path(ServiceConfiguration.SVC_MEASUREMENT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean createSpecification(@QueryParam("token") String usertoken,
									   @QueryParam("specname") String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}
		
		if (existSpecification(specificationName, u)) {
			LOGGER.warn("Specification with the name '{}' already exists.", specificationName);
			return false;
		}

		MeasurementSpecificationBuilder msb = u.getCurrentScenarioDefinitionBuilder()
													  .addNewMeasurementSpecification();
		if (msb == null) {
			LOGGER.warn("Error adding new specification with name '{}'", specificationName);
			return false;
		}

		msb.setName(specificationName);
		
		// store the scenario definition in the databse of the current user
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return false;
		} else {
			ScenarioDefinition scenarioDef = u.getCurrentScenarioDefinitionBuilder().getBuiltScenario();
			dbCon.store(scenarioDef);
		}
		
		// save the selected specification in the service-db
		ServicePersistenceProvider.getInstance().storeUser(u);

		return true;
	}
	
	/**
	 * Renames the current selected MS. Fails if the user has not MS currently selected.
	 * 
	 * @param usertoken the user identification
	 * @param specname the new MS name
	 * @return true, if the current selected MS can be renamed. False, if the user has no
	 * 		   MS currently selected.
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean renameWorkingSpecification(@QueryParam("token") String usertoken,
											  @QueryParam("specname") String specname) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}
		
		if (existSpecification(specname, u)) {
			LOGGER.warn("Can't rename, because specification with the name '{}' already exists.", specname);
			return false;
		}

		MeasurementSpecification msss = u.getCurrentScenarioDefinitionBuilder().getMeasurementSpecification(u.getMeasurementSpecification());

		if (msss == null) {
			LOGGER.warn("User has no MeasurementSpecification selected. Therefore a renaming cannot be completed.");
			return false;
		}
		
		u.setMeasurementSpecification(specname);
		
		// store the scenario definition in the databse of the current user
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found for the user with the token '{}'.", usertoken);
			return false;
		}
		
		ScenarioDefinition scenarioDef = u.getCurrentScenarioDefinitionBuilder().getBuiltScenario();
		dbCon.store(scenarioDef);

		// store the new user data in it's database
		ServicePersistenceProvider.getInstance().storeUser(u);

		return true;
	}
	
	
	
	// *************************** (private) HELPER *****************************************
	
	/**
	 * Returns whether a MS with the given name exists.
	 * 
	 * @param specification MS name
	 * @return true, if MS with the given name exists
	 */
	private boolean existSpecification(String specification, Users u) {

		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder()
										    .getBuiltScenario()
										    .getMeasurementSpecifications()) {
			if (specification.equals(ms.getName())) {
				return true;
			}
		}
		
		return false;
	}
}
