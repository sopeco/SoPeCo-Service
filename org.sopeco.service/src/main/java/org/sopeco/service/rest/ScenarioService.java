package org.sopeco.service.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.builder.ScenarioDefinitionBuilder;

@Path(ServiceConfiguration.SVC_SCENARIO)
public class ScenarioService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioService.class);
	
	/**
	 * Adds a new scenario with the given values. This method DOES NOT switch to the
	 * newly created scenario. The scenario must be switched manually via the service
	 * at @Code{switch}.
	 * 
	 * @param scenarioName the scenario name
	 * @param specificationName the measurment specification name
	 * @param usertoken the user identification
	 * @param esd the @Code{ExperimentSeriesDefinition}
	 * @return true, if the scenario was added succesfully. False if a scenario
	 * 		   with the given already exists.
	 */
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD + "/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean addScenario(@PathParam("name") String scenarioName,
							   @QueryParam("specname") String specificationName,
							   @QueryParam("token") String usertoken,
							   ExperimentSeriesDefinition esd) {
		
		scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");
		
		// check if a scenario with the given name already exsists
		if (loadScenarioDefinition(scenarioName, usertoken) != null) {
			LOGGER.warn("A scenario with the given name '{}' already exsits!", scenarioName);
			return false;
		}
		
		ScenarioDefinitionBuilder sdb = new ScenarioDefinitionBuilder(scenarioName);
		ScenarioDefinition emptyScenario = sdb.getScenarioDefinition();

		if (specificationName != null) {
			emptyScenario.getMeasurementSpecifications().get(0).setName(specificationName);
			
			if (esd != null) {
				emptyScenario.getMeasurementSpecifications().get(0).getExperimentSeriesDefinitions().add(esd);
			}
			
		}
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);

		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return false;
		}
		
		try {
			
			for (ScenarioDefinition sd : dbCon.loadAllScenarioDefinitions()) {
				if (sd.getScenarioName().equals(scenarioName)) {
					LOGGER.info("Scenario with the given name alaready exists");
					return false;
				}
			}
	
		} catch (DataNotFoundException e) {
			return false;
		}
		
		dbCon.store(emptyScenario);
		dbCon.closeProvider();
		
		return true;
	}
	
	/**
	 * Adds a new scenario with a completed scenario object as it. The scenario logic is
	 * not yet checked in this method.
	 * This method DOES NOT switch to the newly created scenario. The scenario must be
	 * switched manually via the service at @Code{switch}.
	 * 
	 * @param usertoken the user identification
	 * @param scenario the scenario as a completed object
	 * @return true, if the scenario was added succesfully. False, if a scenario with
	 * 		   the given name already exists.
	 */
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean addScenario(@QueryParam("token") String usertoken,
							   ScenarioDefinition scenario) {
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		String scenarioname = scenario.getScenarioName();
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return false;
		}
		

		try {
			
			for (ScenarioDefinition sd : dbCon.loadAllScenarioDefinitions()) {
				if (sd.getScenarioName().equals(scenarioname)) {
					LOGGER.info("Scenario with the given name '{}' alaready exists", scenarioname);
					return false;
				}
			}
			
		} catch (DataNotFoundException e) {
			return false;
		}

		dbCon.store(scenario);
		dbCon.closeProvider();
		
		return true;
	}
	
	/**
	 * Return a list with all the scenario names.
	 * 
	 * @param usertoken the user identification
	 * @return a list with all the scenario names
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String[] getScenarioNames(@QueryParam("token") String usertoken) {

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);

		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return null;
		}

		List<ScenarioDefinition> scenarioList;
		
		try {
			scenarioList = dbCon.loadAllScenarioDefinitions();
		} catch (DataNotFoundException e) {
			LOGGER.info("No scenario definitions in database.");
			return null;
		}
		
		String[] retValues = new String[scenarioList.size()];

		for (int i = 0; i < scenarioList.size(); i++) {
			ScenarioDefinition sd = scenarioList.get(i);
			retValues[i] = sd.getScenarioName();
		}
		
		dbCon.closeProvider();
		
		return retValues;
	}
	
	/**
	 * Deleted the scenario with the given name. Does not delete the scenario, if the
	 * given user has currenlty selected the scenario.
	 * 
	 * @param scenarioname the scenario name
	 * @param usertoken the user identification
	 * @return true, if scenario has been deleted. False, if the scenario name is invalid || a scenario
	 *         with the given name is not found || the user has currently selected the scenario
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_SCENARIO_DELETE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeScenario(@QueryParam("name") String scenarioname,
								  @QueryParam("token") String usertoken) {
		
		if (!scenarioname.matches("[a-zA-Z0-9_]+")) {
			return false;
		}

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}
		
		if (u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName() == scenarioname) {
			LOGGER.warn("Can't delete current selected scenario. First must switch to another one.");
			return false;
		}

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		try {
			
			ScenarioDefinition definition = dbCon.loadScenarioDefinition(scenarioname);
			dbCon.remove(definition);
			return true;
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Scenario with name '{}' not found.", scenarioname);
			return false;
		} finally {
			dbCon.closeProvider();
		}
		
	}

	/**
	 * Switch the activ scenario for a given user (via token).
	 * 
	 * @param scenarioname the scenario to switch to
	 * @param usertoken the token to identify the user
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean switchScenario(@QueryParam("name") String scenarioname,
	  							  @QueryParam("token") String usertoken) {
		
		ScenarioDefinition definition = loadScenarioDefinition(scenarioname, usertoken);
		if (definition == null) {
			return false;
		}

		ScenarioDefinitionBuilder builder = new ScenarioDefinitionBuilder(definition);
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}
		
		u.setCurrentScenarioDefinitionBuilder(builder);
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return true;
	}
	
	
	/**************************************HELPER****************************************/
	
	/**
	 * Load a Scenario definition with the given name and user (via token).
	 * 
	 * @param scenarioname the name of the scenario which definition has to be loaded
	 * @param token the token to identify the user
	 * @return The scenario definition for the scenario with the given name. Null if there
	 * 			is no scenario with the given name.
	 */
	private ScenarioDefinition loadScenarioDefinition(String scenarioname, String token) {

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(token);
		
		try {
			
			ScenarioDefinition definition = dbCon.loadScenarioDefinition(scenarioname);
			return definition;
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Scenario '{}' not found.", scenarioname);
			return null;
		}
	}
	
}
