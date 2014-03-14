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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
import org.sopeco.persistence.entities.definition.ExplorationStrategy;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.ScenarioDetails;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.builder.ScenarioDefinitionBuilder;

/**
 * The <code>ScenarioService</code> class provides RESTful services to handle scenarios in SoPeCo.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_SCENARIO)
public class ScenarioService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioService.class.getName());
	
	private static final String TOKEN = ServiceConfiguration.SVCP_SCENARIO_TOKEN;
	
	private static final String NAME = ServiceConfiguration.SVCP_SCENARIO_NAME;
	
	/**
	 * Adds a new scenario with the given values. This method <b>DOES NOT</b> switch to the
	 * newly created scenario. The scenario must be switched manually via the service
	 * at {@link #switchScenario(String, String)}.<br />
	 * <br />
	 * To have a correct created scenario, the {@link ExperimentSeriesDefinition} must have
	 * a non-null {@link ExplorationStrategy} added. If it's still null, when attempting to
	 * add a scenario, a new empty {@code ExplorationStrategy} is added automatically.
	 * 
	 * @param scenarioName 		the scenario name
	 * @param specificationName the measurment specification name
	 * @param usertoken	 		the user identification
	 * @param esd 				the {@link ExperimentSeriesDefinition}
	 * @return 					{@link Response} OK, CONFLICT, UNAUTHORIZED or
	 * 							INTERNAL_SERVER_ERROR<br />
	 * 							OK if scenario was added succesfully
	 */
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD + "/{" + NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addScenario(@PathParam(NAME) String scenarioName,
							    @QueryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME) String specificationName,
							    @QueryParam(TOKEN) String usertoken,
							    ExperimentSeriesDefinition esd) {
		
		LOGGER.debug("Checking scenario adding (scenario name '{}').", scenarioName);
		
		scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");
		
		// check if a scenario with the given name already exsists
		if (loadScenarioDefinition(scenarioName, usertoken) != null) {
			LOGGER.info("A scenario with the given name '{}' already exsits!", scenarioName);
			return Response.status(Status.CONFLICT).entity("scenario name already exsits").build();
		}
		
		ScenarioDefinitionBuilder sdb = new ScenarioDefinitionBuilder(scenarioName);
		//sdb.getMeasurementSpecificationBuilder().addExperimentSeries(esd);
		ScenarioDefinition emptyScenario = sdb.getScenarioDefinition();

		if (specificationName == null || specificationName.equals("")) {
			LOGGER.info("Specification name is invalid.");
			return Response.status(Status.CONFLICT).entity("Specification name is invalid.").build();
		}
		
		if (esd == null) {
			LOGGER.info("ExperimentSeriesDefinition is invalid.");
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition is invalid.").build();
		}
		
		// check if ExperimentSeriesDefinitions has an ExplorationStrategy added
		if (esd.getExplorationStrategy() == null) {
			esd.setExplorationStrategy(new ExplorationStrategy());
		}
		
		// now replace the default created MeasurementSpecification with the custom one
		int defaultIndexMS = 0;
		int defaultIndexESD = 0;
		MeasurementSpecification ms = new MeasurementSpecification();
		ms.getExperimentSeriesDefinitions().add(esd);
		ms.setName(specificationName);
		emptyScenario.getMeasurementSpecifications().set(defaultIndexMS, ms);
		emptyScenario.getMeasurementSpecifications().get(defaultIndexMS).getExperimentSeriesDefinitions().set(defaultIndexESD, esd);
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);

		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return Response.status(Status.UNAUTHORIZED).build();
		}

		LOGGER.debug("Adding scenario with name '{}' to database.", scenarioName);
		
		try {
			
			for (ScenarioDefinition sd : dbCon.loadAllScenarioDefinitions()) {
				if (sd.getScenarioName().equals(scenarioName)) {
					LOGGER.info("Scenario with the given name already exists. Aborting database adding.");
					return Response.status(Status.CONFLICT).entity("scenario name already exsits").build();
				}
			}
	
		} catch (DataNotFoundException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		dbCon.store(emptyScenario);
		
		dbCon.closeProvider();	
		
		LOGGER.debug("Scenario with name '{}' stored database.", scenarioName);
		
		return Response.ok().build();
	}
	
	/**
	 * Adds a new scenario with a given {@link ScenarioDefinition} as it. The scenario
	 * logic is not yet checked in this method.
	 * This method DOES NOT switch to the newly created scenario. The scenario must be
	 * switched manually via the service at {@link #switchScenario(String, String)}.
	 * 
	 * @param usertoken the user identification
	 * @param scenario 	the scenario as a completed object
	 * @return 			{@link Response} OK, CONFLICT, UNAUTHORIZED or
	 * 					INTERNAL_SERVER_ERROR<br />
	 * 					OK if scenario was added succesfully
	 */
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addScenario(@QueryParam(TOKEN) String usertoken,
							   	ScenarioDefinition scenario) {
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		String scenarioname = scenario.getScenarioName();
		
		if (dbCon == null) {
			// this can be thrown by a wrong token, too
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		// test the scenario for non-null values (only in first entry)
		if (scenario.getAllExperimentSeriesDefinitions() == null) {
			LOGGER.info("ExperimentSeriesDefinition list is invalid.");
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition list is invalid.").build();
		}
		
		for (ExperimentSeriesDefinition esd : scenario.getAllExperimentSeriesDefinitions()) {
			
			if (esd == null) {
				LOGGER.info("An ExperimentSeriesDefinition in list is invalid.");
				return Response.status(Status.CONFLICT).entity("An ExperimentSeriesDefinition in list is invalid.").build();
			} 
			
			if (esd.getExplorationStrategy() == null) {
				LOGGER.info("ExplorationStrategy is invalid.");
				return Response.status(Status.CONFLICT).entity("ExplorationStrategy is invalid.").build();
			}
			
		}
		
		// now check if there is already a scenario with the given name
		try {
			
			for (ScenarioDefinition sd : dbCon.loadAllScenarioDefinitions()) {
				if (sd.getScenarioName().equals(scenarioname)) {
					LOGGER.info("Scenario with the given name '{}' alaready exists", scenarioname);
					return Response.status(Status.CONFLICT).entity("Scenario with given name already exists.").build();
				}
			}
			
		} catch (DataNotFoundException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		dbCon.store(scenario);
		dbCon.closeProvider();
		
		return Response.ok().build();
	}
	
	/**
	 * Return a list with all the scenario names.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR<br />
	 * 					OK has a String[] as {@link Entity}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioNames(@QueryParam(TOKEN) String usertoken) {

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);

		if (dbCon == null) {
			LOGGER.warn("Invalid token '{}'", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		List<ScenarioDefinition> scenarioList;
		
		try {
			scenarioList = dbCon.loadAllScenarioDefinitions();
		} catch (DataNotFoundException e) {
			LOGGER.info("Fetching scenario list from database failed.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		String[] retValues = new String[scenarioList.size()];

		for (int i = 0; i < scenarioList.size(); i++) {
			ScenarioDefinition sd = scenarioList.get(i);
			retValues[i] = sd.getScenarioName();
		}
		
		dbCon.closeProvider();

		return Response.ok(retValues).build();
	}
	
	/**
	 * Returns the current selected {@code ScenarioDefinition} for the user.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR<br />
	 * 					OK with a {@code ScenarioDefinition} as {@link Entity}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_CURRENT)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrentScenario(@QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition tmpSD = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();

		return Response.ok(tmpSD).build();
	}
	
	/**
	 * Deleted the scenario with the given name. Fails to delete the scenario, if the
	 * given user has currently selected the scenario.
	 * <b>Attention:</b> All the {@link ScenarioInstance}s related to the scenario will
	 * be deleted, too!
	 * 
	 * @param scenarioname 	the scenario name
	 * @param usertoken 	the user identification
	 * @return 				{@link Response} OK, UNAUTHORIZED, CONFLICT, ACCEPTED or
	 * 						NO_CONTENT<br />
	 * 						ACCEPTED can occur, when it's tried to delete
	 * 						the currently selected scenario<br />
	 * 						NO_CONTENT indicates, that a scenario with the
	 * 						given name cannot be found in the database
	 */
	@DELETE
	@Path("{" + NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeScenario(@PathParam(NAME) String scenarioname,
								   @QueryParam(TOKEN) String usertoken) {
		
		if (!scenarioname.matches("[a-zA-Z0-9_]+")) {
			return Response.status(Status.CONFLICT).entity("Scenario name must match pattern [a-zA-Z0-9_]+").build();
		}

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		// if the string comparison is made with equals(), two test cases fail!
		if (u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName() == scenarioname) {
			LOGGER.warn("Can't delete the current selected scenario. First must switch to another one.");
			return Response.status(Status.ACCEPTED).entity("Cannot delete current selected scenario.").build();
		}
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		try {
			
			ScenarioDefinition definition = dbCon.loadScenarioDefinition(scenarioname);
			
			// check for scenario Instances and remove these
			if (definition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return Response.status(Status.CONFLICT).entity("ScenarioDefinition is invalid.").build();
			}
			
			List<ScenarioInstance> scenarioInstances = dbCon.loadScenarioInstances(scenarioname);
			
			if (scenarioInstances == null) {
				LOGGER.warn("ScenarioInstances cannot be fetched.");
				return Response.status(Status.CONFLICT).entity("No scenario instances for given scenario in database.").build();
			}
			
			for (ScenarioInstance si : scenarioInstances) {
				dbCon.remove(si);
			}
			
			dbCon.remove(definition);
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Scenario with name '{}' not found.", scenarioname);
			return Response.status(Status.NO_CONTENT).entity("Scenario with given name does not exist.").build();
		} finally {
			dbCon.closeProvider();
		}
		
		ServicePersistenceProvider.getInstance().storeUser(u);

		return Response.ok().build();
	}

	/**
	 * Switch the activ scenario for a given user (via token).<br />
	 * <br />
	 * This method has not a real REST URL, because there exists two methods
	 * for siwtching the scenario: One with the name as String and one with an
	 * {@link ScenarioDefinition}. Both need to map to unique URLs and therefore
	 * this one maps to switch/name?name="" and the other one to switch/definition.
	 * 
	 * @param scenarioname 	the scenario to switch to
	 * @param usertoken 	the token to identify the user
	 * @return				{@link Response} OK, UNAUTHORIZED, INTERNAL_SERVER_ERROR or
	 * 						NO_CONTENT<br />
	 * 						NO_CONTENT is thrown, when the given name cannot be matched to
	 * 						a scenario in the database
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_SWITCH
			+ "/" + ServiceConfiguration.SVC_SCENARIO_SWITCH_NAME)
	@Produces(MediaType.APPLICATION_JSON)
	public Response switchScenario(@QueryParam(NAME) String scenarioname,
								   @QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition definition = loadScenarioDefinition(scenarioname, usertoken);
		if (definition == null) {
			return Response.status(Status.NO_CONTENT).entity("No scenario with given name exists.").build();
		}
		
		ScenarioDefinitionBuilder builder = new ScenarioDefinitionBuilder(definition);
		
		u.setCurrentScenarioDefinitionBuilder(builder);
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection for user found.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		dbCon.store(sd);
		dbCon.closeProvider();
		
		// update the account details of the account
		setAccountDetails(usertoken, sd);
		
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return Response.ok().build();
	}

	/**
	 * Stores all results of the {@code ScenarioInstance}s of the current connected
	 * account. The results are archived and stay in the database in an own table. 
	 * 
	 * @param usertoken the token to identify the user
	 * @return 			{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_SCENARIO_STORE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response storeScenario(@QueryParam(TOKEN) String usertoken,
								  ScenarioDefinition definition) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
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
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		dbCon.closeProvider();
		
		// as the user has not changed, this store is not necessary
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns the current scenario written down in XML. Not the XML is passed back, but
	 * a String.
	 * 
	 * @param usertoken the token to identify the user
	 * @return 			{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 					OK with {@link String} {@link Entity}, the scenario in XML
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioAsXML(@QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition definition = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();

		if (definition == null) {
			LOGGER.info("User has no scenario selected!");
			return Response.status(Status.CONFLICT).entity("User has no scenario selected!").build();
		}
		
		ScenarioDefinitionWriter writer = new ScenarioDefinitionWriter(usertoken);
		String xml = writer.convertToXMLString(definition);
		
		return Response.ok(xml).build();
	}
	
	/**
	 * Returns the {@link ScenarioInstance} identified with the given name and url.
	 * 
	 * @param usertoken the token to identify the user
	 * @param name		the name of the {@link ScenarioInstance}
	 * @param url		the URL of the MeasurementEnvironmentController
	 * @return			{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 					OK with {@link ScenarioInstance} as {@link Entity} (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_INSTANCE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioInstance(@QueryParam(TOKEN) String usertoken,
										@QueryParam(ServiceConfiguration.SVCP_SCENARIO_NAME) String name,
										@QueryParam(ServiceConfiguration.SVCP_SCENARIO_URL) String url) {
			
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			ScenarioInstance tmpSI = UserPersistenceProvider.createPersistenceProvider(usertoken).loadScenarioInstance(name, url);
			return Response.ok(tmpSI).build();
			
		} catch (DataNotFoundException e) {
			
			LOGGER.info("Cannot find a scenario definition with name '{}' and URL '{}'.", name, url);
			return Response.status(Status.CONFLICT).entity("Cannot find a scenario definition with given name and URL.").build();
			
		}
	}
	
	/**
	 * Returns all the {@link ScenarioInstance}s to the scenario with the given name, related to the account
	 * with the given token.
	 * 
	 * @param usertoken the token to identify the user
	 * @param name		the name of the {@link ScenarioInstance}
	 * @return			{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 					OK with List<{@link ScenarioInstance}s> as {@link Entity}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_INSTANCES)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioInstances(@QueryParam(TOKEN) String usertoken,
										 @QueryParam(ServiceConfiguration.SVCP_SCENARIO_NAME) String name) {
			
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			List<ScenarioInstance> listSI = UserPersistenceProvider.createPersistenceProvider(usertoken).loadScenarioInstances(name);
			return Response.ok(listSI).build();
			
		} catch (DataNotFoundException e) {
			
			LOGGER.info("Cannot find a scenario definition with name '{}'.", name);
			return Response.status(Status.CONFLICT).entity("Cannot find a scenario definition with given name.").build();
			
		}
	}
	
	/**
	 * Returns the {@link ScenarioDefinition} the current user has.
	 * 
	 * @param usertoken the token to identify the user
	 * @param name		the name of the {@link ScenarioInstance}
	 * @param url		the URL of the MeasurementEnvironmentController
	 * @return			{@link Response} OK or UNAUTHORIZED<br />
	 * 					OK with {@link ScenarioDefinition} as {@link Entity} (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_SCENARIO_DEFINITON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioDefinition(@QueryParam(TOKEN) String usertoken) {
			
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		return Response.ok(u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition()).build();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// HELPER /////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Load a Scenario definition with the given name and user (via token).
	 * 
	 * @param scenarioname 	the name of the scenario which definition has to be loaded
	 * @param token 		the token to identify the user
	 * @return 				The scenario definition for the scenario with the given name.
	 * 						Null if there is no scenario with the given name.
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
	 * Update the {@link AccountDetails} for the account connected to the {@link Users} with the given
	 * token. The given {@link ScenarioDefinition} is actually only needed for the scenario name.<br />
	 * The {@link AccountDetails} will have the scenario name as selected scenario name.<br />
	 * Afterwards the {@link AccountDetails} will be in the database.
	 * 
	 * @param usertoken				the token to identify the user
	 * @param scenarioDefinition	the {@link ScenarioDefinition}
	 */
	private void setAccountDetails(String usertoken, ScenarioDefinition scenarioDefinition) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Given token '{}' is invalid.", usertoken);
			return;
		}
		
		String scenarioname = scenarioDefinition.getScenarioName();
		AccountDetails ad 	= u.getAccountDetails();
		
		// check if scenario is already in account scenario detail list
		boolean scenarioExists = false;
		for (ScenarioDetails scenarioDetail : ad.getScenarioDetails()) {
			
			if (scenarioDetail.getScenarioName().equals(scenarioname)) {
				// scenario detail is already in list and don't need to be created
				scenarioExists = true;
			}
			
		}
		
		if (!scenarioExists) {
			// must create the ScenarioDetails now
			ScenarioDetails scenarioDetail = new ScenarioDetails();
			scenarioDetail.setScenarioName(scenarioname);
			scenarioDetail.setSelectedSpecification("");
			scenarioDetail.setSelectedExperiment("");
			ad.getScenarioDetails().add(scenarioDetail);
		}

		ad.setSelectedScenario(scenarioname);
		
		ServicePersistenceProvider.getInstance().storeAccountDetails(ad);
	}
	
	/**
	 * Archiving old scenario results from the given {@code ScenarioInstance} into the
	 * account database.
	 * 
	 * @param u the user who wants to archive
	 * @param 	scenarioInstance the scenario to save
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
