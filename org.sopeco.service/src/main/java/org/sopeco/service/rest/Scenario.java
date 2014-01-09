package org.sopeco.service.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
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
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.user.UserManager;
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
							   ExperimentSeriesDefinition esd) {
		
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

		dbCon.store(emptyScenario);

		switchScenario(scenarioName, usertoken);
		return true;
	}
	
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Boolean addScenario(@QueryParam("token") String usertoken,
							   ScenarioDefinition scenario) {
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);

		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return false;
		}

		dbCon.store(scenario);

		switchScenario(scenario.getScenarioName(), usertoken);
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
	private boolean switchScenario(String scenarioname, String token) {
		
		ScenarioDefinition definition = loadScenarioDefinition(scenarioname, token);
		if (definition == null) {
			return false;
		}

		ScenarioDefinitionBuilder builder = ScenarioDefinitionBuilder.load(definition);
		UserManager.instance().getUser(token).setCurrentScenarioDefinitionBuilder(builder);

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
		try {
			ScenarioDefinition definition = UserPersistenceProvider.createPersistenceProvider(token).loadScenarioDefinition(scenarioname);

			return definition;
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Scenario '{}' not found.", scenarioname);
			return null;
		}
	}
	
}
