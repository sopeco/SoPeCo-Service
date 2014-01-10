package org.sopeco.service.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
public class Scenario {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Scenario.class);
	
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD + "/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Boolean addScenario(@PathParam("name") String scenarioName,
							   @QueryParam("specname") String specificationName,
							   @QueryParam("token") String usertoken,
							   ExperimentSeriesDefinition esd) throws DataNotFoundException {
		
		scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");

		
		
		ScenarioDefinition emptyScenario = ScenarioDefinitionBuilder.buildEmptyScenario(scenarioName);

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

		switchScenarioHelper(scenarioName, usertoken);
		return true;
	}
	
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Boolean addScenario(@QueryParam("token") String usertoken,
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
		switchScenarioHelper(scenarioname, usertoken);
		
		return true;
	}
	
	@POST
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
	
	@DELETE
	@Path(ServiceConfiguration.SVC_SCENARIO_DELETE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeScenario(@QueryParam("name") String scenarioname,
								  @QueryParam("token") String usertoken) {
		
		if (!scenarioname.matches("[a-zA-Z0-9_]+")) {
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

	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean switchScenario(@QueryParam("name") String scenarioname,
	  							  @QueryParam("token") String usertoken) {
		
		ScenarioDefinition definition = loadScenarioDefinition(scenarioname, usertoken);
		if (definition == null) {
			return false;
		}

		ScenarioDefinitionBuilder builder = ScenarioDefinitionBuilder.load(definition);
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		u.setCurrentScenarioDefinitionBuilder(builder);
		ServicePersistenceProvider.getInstance().storeUser(u);

		return true;

	}
	
	
	/*******************HELPER**************************/
	
	/**
	 * Switch the activ scenario for a given user (via token).
	 * 
	 * @param scenarioname the scenario to switch to
	 * @param token the token to identify the user
	 * @return true, if the scenario could be switched
	 */
	private boolean switchScenarioHelper(String scenarioname, String token) {
		
		ScenarioDefinition definition = loadScenarioDefinition(scenarioname, token);
		if (definition == null) {
			return false;
		}

		ScenarioDefinitionBuilder builder = ScenarioDefinitionBuilder.load(definition);
		Users u = ServicePersistenceProvider.getInstance().loadUser(token);
		u.setCurrentScenarioDefinitionBuilder(builder);
		ServicePersistenceProvider.getInstance().storeUser(u);

		return true;

	}
	
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
		} finally {
			dbCon.closeProvider();
		}
		
	}
	
}
