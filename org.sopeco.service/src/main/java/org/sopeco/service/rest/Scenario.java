package org.sopeco.service.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.user.UserManager;
import org.sopeco.service.builder.ScenarioDefinitionBuilder;

@Path(ServiceConfiguration.SVC_SCENARIO)
public class Scenario {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Scenario.class);
	
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Boolean addScenario(@QueryParam("name") String scenarioName,
							   @QueryParam("specname") String specificationName,
							   @QueryParam("experiment") String experiment,
							   @QueryParam("token") String token) {
		
		// map the experiment to ExperimentSeriesDefinition with jackson
		ObjectMapper mapper = new ObjectMapper();
		ExperimentSeriesDefinition esd;
		try {
			esd = mapper.readValue(experiment, ExperimentSeriesDefinition.class);
		} catch (Exception e) {
			LOGGER.info("Could not map param experiments into a valid ExperimentSeriesDefinition!");
			return false;
		}
		
		scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");

		ScenarioDefinition emptyScenario = ScenarioDefinitionBuilder.buildEmptyScenario(scenarioName);

		if (specificationName != null) {
			emptyScenario.getMeasurementSpecifications().get(0).setName(specificationName);
			
			if (experiment != null) {
				emptyScenario.getMeasurementSpecifications().get(0).getExperimentSeriesDefinitions().add(esd);
			}
			
		}

		IPersistenceProvider dbCon = UserManager.instance().getUser(token).getCurrentPersistenceProvider();

		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return false;
		}

		dbCon.store(emptyScenario);

		switchScenario(scenarioName, token);
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
			ScenarioDefinition definition = UserManager.instance().getUser(token)
												.getCurrentPersistenceProvider()
												.loadScenarioDefinition(scenarioname);

			return definition;
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Scenario '{}' not found.", scenarioname);
			return null;
		}
	}
	
}
