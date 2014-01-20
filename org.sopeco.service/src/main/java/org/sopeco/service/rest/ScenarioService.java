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
import org.sopeco.config.Configuration;
import org.sopeco.config.IConfiguration;
import org.sopeco.engine.model.ScenarioDefinitionWriter;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.ArchiveEntry;
import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
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
	
	private static final String TOKEN = ServiceConfiguration.SVCP_SCENARIO_TOKEN;
	
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
							   @QueryParam(TOKEN) String usertoken,
							   ExperimentSeriesDefinition esd) {

		scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");
		
		// check if a scenario with the given name already exsists
		if (loadScenarioDefinition(scenarioName, usertoken) != null) {
			LOGGER.info("A scenario with the given name '{}' already exsits!", scenarioName);
			return false;
		}
		
		ScenarioDefinitionBuilder sdb = new ScenarioDefinitionBuilder(scenarioName);
		//sdb.getMeasurementSpecificationBuilder().addExperimentSeries(esd);
		ScenarioDefinition emptyScenario = sdb.getScenarioDefinition();

		if (specificationName == null || specificationName.equals("")) {
			LOGGER.info("Specification name is invalid.");
			return false;
		}
		
		if (esd == null) {
			LOGGER.info("ExperimentSeriesDefinition is invalid.");
			return false;
		}
		
		// now replace the default created MeasurementSpecification with the custom one
		int defaultIndexMS = 0;
		int defaultIndexESD = 0;
		MeasurementSpecification ms = new MeasurementSpecification();
		ms.getExperimentSeriesDefinitions().add(esd);
		ms.setName(specificationName);
		emptyScenario.getMeasurementSpecifications().set(defaultIndexMS, ms);
		emptyScenario.getMeasurementSpecifications().get(defaultIndexMS).getExperimentSeriesDefinitions().set(defaultIndexESD, esd);
		
		
		//int index = emptyScenario.getMeasurementSpecifications().indexOf(ms);
		//System.out.println("Measurementname: " + emptyScenario.getMeasurementSpecifications().get(1).getName());
		//emptyScenario.getMeasurementSpecifications().get(index).getExperimentSeriesDefinitions().add(esd);
		//sdb.getMeasurementSpecificationBuilder().addExperimentSeries(esd);
		
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
		
		try {
			emptyScenario = dbCon.loadScenarioDefinition(emptyScenario.getScenarioName());
			System.out.println("t3t3t: " + emptyScenario.getMeasurementSpecifications().get(0).getExperimentSeriesDefinitions().get(0).getName());
		} catch (DataNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	public boolean addScenario(@QueryParam(TOKEN) String usertoken,
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
	public String[] getScenarioNames(@QueryParam(TOKEN) String usertoken) {

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
	 * Returns the current selected {@code ScenarioDefinition} for the user.
	 * 
	 * @param usertoken the user identification
	 * @return the current selected {@code ScenarioDefinition} for the user
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
	@Produces(MediaType.APPLICATION_JSON)
	public ScenarioDefinition getCurrentScenario(@QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		return u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
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
								  @QueryParam(TOKEN) String usertoken) {
		
		if (!scenarioname.matches("[a-zA-Z0-9_]+")) {
			return false;
		}

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}
		
		if (u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName() == scenarioname) {
			LOGGER.warn("Can't delete the current selected scenario. First must switch to another one.");
			return false;
		}

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		try {
			
			ScenarioDefinition definition = dbCon.loadScenarioDefinition(scenarioname);
			dbCon.remove(definition);
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Scenario with name '{}' not found.", scenarioname);
			return false;
		} finally {
			dbCon.closeProvider();
		}
		
		ServicePersistenceProvider.getInstance().storeUser(u);

		return true;
	}

	/**
	 * Switch the activ scenario for a given user (via token).
	 * 
	 * @param scenarioname the scenario to switch to
	 * @param usertoken the token to identify the user
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_SWITCH + "/"
			+ ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean switchScenario(@QueryParam(TOKEN) String usertoken,
								  @QueryParam("name") String scenarioname) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		ScenarioDefinition definition = loadScenarioDefinition(scenarioname, usertoken);
		if (definition == null) {
			return false;
		}

		System.out.println("tttttt: " + definition.getMeasurementSpecifications().get(0).getExperimentSeriesDefinitions().get(0).getName());
		
		ScenarioDefinitionBuilder builder = new ScenarioDefinitionBuilder(definition);
		
		u.setCurrentScenarioDefinitionBuilder(builder);
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection for user found.");
			return false;
		}
		
		dbCon.store(sd);
		dbCon.closeProvider();
		
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return true;
	}
	
	/**
	 * Switches a scenario to another one given by the whole {@code ScenarioDefinition}.
	 * 
	 * @param usertoken the token to identify the user
	 * @param scenarioDefinition the new {@code ScenarioDefinition} to set
	 * @return true, if the scenario could be switched to the given one
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_SWITCH + "/"
			+ ServiceConfiguration.SVC_SCENARIO_SWITCH_DEFINITION)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean switchScenario(@QueryParam(TOKEN) String usertoken,
								  ScenarioDefinition scenarioDefinition) {
		
		if (scenarioDefinition == null) {
			LOGGER.warn("Invalid scenario definition!");
			return false;
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection for user found.");
			return false;
		}
		
		u.setCurrentScenarioDefinitionBuilder(new ScenarioDefinitionBuilder(scenarioDefinition));
		
		// store the new scenario defintion in the database
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		
		dbCon.store(sd);
		dbCon.closeProvider();

		// store user information in the service database
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return true;
	}
	
	/**
	 * Stores all results of the {@code ScenarioInstance}s of the current connected
	 * account. The results are archived and stay in the database in an own table. 
	 * 
	 * @param usertoken the token to identify the user
	 * @return true, if all current scenario instances could be stored
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_ARCHIVE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean storeScenario(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection for user found.");
			return false;
		}
		
		ScenarioDefinition current = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		
		try {
			
			for (ScenarioInstance instance : dbCon.loadScenarioInstances(current.getScenarioName())) {
					
				String changeHandlingMode = Configuration.getSessionSingleton(usertoken).getPropertyAsStr(
						IConfiguration.CONF_DEFINITION_CHANGE_HANDLING_MODE);
				
				if (changeHandlingMode.equals(IConfiguration.DCHM_ARCHIVE)) {
					archiveOldResults(u, instance);
				}

				dbCon.removeScenarioInstanceKeepResults(instance);
				
			}
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Problem loading available scenario instances!", usertoken);
			return false;
		}
		
		dbCon.closeProvider();
		
		// as the user has not changed, this store is not necessary
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return true;
	}
	
	/**
	 * Returns  the current scenario written down in XML. Not the XML is passed back, but
	 * a String.
	 * 
	 * @param usertoken the token to identify the user
	 * @return string in XML format of the scenario
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public String getScenarioAsXML(@QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return "";
		}
		
		ScenarioDefinition definition = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();

		if (definition == null) {
			LOGGER.info("User has not scenario selected!");
			return "";
		}
		
		ScenarioDefinitionWriter writer = new ScenarioDefinitionWriter(usertoken);
		System.out.println("++++++++++++++++++++++++");
		System.out.println(u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName());
		System.out.println(u.getCurrentScenarioDefinitionBuilder().getMeasurementSpecificationBuilder());
		System.out.println("write: " + writer);
		System.out.println("scenariodefinition object: " + definition);
		String xml = writer.convertToXMLString(definition);
		System.out.println("+++++++++++++++++++++++++");
		return xml;
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
	
	/**
	 * Archiving old scenario results from the given {@code ScenarioInstance} into the
	 * account database.
	 * 
	 * @param u the user who wants to archive
	 * @param scenarioInstance the scenario to save
	 */
	private void archiveOldResults(Users u, ScenarioInstance scenarioInstance) {
		
		ScenarioDefinitionWriter writer = new ScenarioDefinitionWriter(u.getToken());
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(u.getToken());
		
		String scenarioDefinitionXML = writer.convertToXMLString(scenarioInstance.getScenarioDefinition());
		
		for (ExperimentSeries es : scenarioInstance.getExperimentSeriesList()) {
			
			for (ExperimentSeriesRun run : es.getExperimentSeriesRuns()) {
				ArchiveEntry entry = new ArchiveEntry(dbCon,
													  run.getTimestamp(),
												  	  scenarioInstance.getName(),
												  	  scenarioInstance.getMeasurementEnvironmentUrl(),
												  	  es.getName(),
												  	  run.getLabel(),
												  	  scenarioDefinitionXML,
												  	  run.getDatasetId());
				
				dbCon.store(entry);
			}
			
		}
		
	}
	
}
