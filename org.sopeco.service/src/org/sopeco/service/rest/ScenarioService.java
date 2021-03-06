/**
 * Copyright (c) 2014 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import org.sopeco.service.helper.ServiceStorageModul;
import org.sopeco.service.helper.SimpleEntityFactory;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.AccountPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.exchange.ExperimentSeriesRunDecorator;

/**
 * The {@link ScenarioService} class provides RESTful services to handle scenarios in SoPeCo.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_SCENARIO)
public class ScenarioService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioService.class.getName());

	/**
	 * Shortener, because the token is passed on nearly every interface.
	 */
	private static final String TOKEN = ServiceConfiguration.SVCP_SCENARIO_TOKEN;
	
	/**
	 * Shortener, because the scenario name is passed on nearly every interface.
	 */
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
	 * @param esd 				the {@link ExperimentSeriesDefinition} with a set name
	 * @return 					{@link Response} OK, CONFLICT or UNAUTHORIZED<br />
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
		
		if (scenarioName == null || specificationName == null || usertoken == null || esd == null) {
			LOGGER.info("One or more parameters are invalid.");
			return Response.status(Status.CONFLICT).entity("One or more parameters are invalid.").build();
		}
		
		LOGGER.debug("Checking scenario adding (scenario name '{}').", scenarioName);
		
		scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");
		
		// check if a scenario with the given name already exsists
		if (ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken) != null) {
			LOGGER.info("A scenario with the given name '{}' already exsits!", scenarioName);
			return Response.status(Status.CONFLICT).entity("scenario name already exsits").build();
		}
		
		LOGGER.debug("Scenario has unusued name.");
		
		if (specificationName.equals("")) {
			LOGGER.info("Specification name is invalid.");
			return Response.status(Status.CONFLICT).entity("Specification name is invalid.").build();
		}

		LOGGER.debug("MeasurementSpecifiation name is valid.");
		
		// adjust the builder for the new scenario
		ScenarioDefinition sd = new ScenarioDefinition();
		
		sd.setScenarioName(scenarioName);
		MeasurementSpecification ms = SimpleEntityFactory.createMeasurementSpecification(specificationName);
		ms.getExperimentSeriesDefinitions().add(esd);
		sd.getMeasurementSpecifications().add(ms);
		sd.setMeasurementEnvironmentDefinition(SimpleEntityFactory.createDefaultMeasurementEnvironmentDefinition());
		
		// check if ExperimentSeriesDefinitions has an ExplorationStrategy added
		if (esd.getExplorationStrategy() == null) {
			esd.setExplorationStrategy(new ExplorationStrategy());
		}
		
		LOGGER.debug("Scenario configured.");
		
		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}
		
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
		
		if (scenario == null) {
			LOGGER.warn("Given ScenarioDefinition is null.");
			return Response.status(Status.CONFLICT).build();
		}
		
		String scenarioname = scenario.getScenarioName();
		
		if (scenarioname.equals("")) {
			LOGGER.info("Invalid scenario name. The name must be not empty.");
			return Response.status(Status.CONFLICT).entity("Invalid scenario name. The name must be not empty.").build();
		}
		
		// check if a scenario with the given name already exsists
		if (ServiceStorageModul.loadScenarioDefinition(scenario.getScenarioName(), usertoken) != null) {
			LOGGER.info("A scenario with the given name '{}' already exsits!", scenario.getScenarioName());
			return Response.status(Status.CONFLICT).entity("scenario name already exsits").build();
		}
		
		// test the scenario for non-null values (only in first entry)
		if (scenario.getMeasurementSpecifications() == null) {
			LOGGER.info("List of MeasurementSpecification is invalid.");
			return Response.status(Status.CONFLICT).entity("List of MeasurementSpecification is invalid.").build();
		}
		
		if (scenario.getMeasurementSpecifications().get(0).getName().equals("")) {
			LOGGER.info("MeasurementSpecification (index 0) name is invalid.");
			return Response.status(Status.CONFLICT).entity("MeasurementSpecification (index 0) name is invalid.").build();
		}
		
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

		if (!ServiceStorageModul.storeScenarioDefition(usertoken, scenario)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}
		
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

		if (usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(usertoken);

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
	 * Deleted the scenario with the given name.
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
		
		if (scenarioname == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		LOGGER.debug("Try to remove scenario with name '{}'.", scenarioname);
		
		if (!scenarioname.matches("[a-zA-Z0-9_]+")) {
			return Response.status(Status.CONFLICT).entity("Scenario name must match pattern [a-zA-Z0-9_]+").build();
		}

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(usertoken);
		
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

			LOGGER.debug("Removal of ScenarioDefinition with name '{}' succeeded.", scenarioname);
			
		} catch (DataNotFoundException e) {
			
			LOGGER.warn("Scenario with name '{}' not found.", scenarioname);
			return Response.status(Status.NO_CONTENT).entity("Scenario with given name does not exist.").build();
			
		} finally {
			
			dbCon.closeProvider();
			
		}

		return Response.ok().build();
	}

	/**
	 * Archives all results of the {@link ScenarioInstance}s of the current connected
	 * account. The results are archived and stay in the database in an own table. 
	 * 
	 * @param usertoken the token to identify the user
	 * @return 			{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_SCENARIO_ARCHIVE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response archiveScenario(@QueryParam(TOKEN) String usertoken,
								  	@PathParam(NAME) String scenarioDefinitionName) {
		
		if (scenarioDefinitionName == null) {
			LOGGER.info("ScenarioDefinition name is null and therefor invalid.");
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition name is null and therefor invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.info("Cannot fetch database connection with given token.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot fetch database connection with given token.").build();
		}
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioDefinitionName, usertoken);
		
		if (sd == null) {
			LOGGER.info("ScenarioDefinition name does not match as ScenarioDefintiion.");
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition name does not match as ScenarioDefintiion.").build();
		}
		
		try {
			
			for (ScenarioInstance instance : dbCon.loadScenarioInstances(sd.getScenarioName())) {
					
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
		
		return Response.ok().build();
	}
	
	/**
	 * Stores the {@link ScenarioDefinition} in the database. This overwrites the database entity
	 * of a {@link ScenarioDefinition} with the same name.
	 * 
	 * @param usertoken				the token to identify the user
	 * @param scenarioDefinition	the {@link ScenarioDefinition}
	 * @return						{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@POST
	@Path(ServiceConfiguration.SVC_SCENARIO_UPDATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateScenario(@QueryParam(TOKEN) String usertoken,
								  ScenarioDefinition scenarioDefinition) {
		
		if (usertoken == null) {
			return Response.status(Status.CONFLICT).entity("Usertoken is null.").build();
		}
		
		LOGGER.debug("Updating the ScenarioDefinition.");
		
		if (scenarioDefinition == null) {
			LOGGER.info("Invalid ScenarioDefinition!");
			return Response.status(Status.CONFLICT).entity("Invalid ScenarioDefinition!").build();
		}
		
		if (scenarioDefinition.getScenarioName().equals("")) {
			LOGGER.info("Invalid ScenarioDefinition name!");
			return Response.status(Status.CONFLICT).entity("Invalid ScenarioDefinition name!").build();
		}
		
		LOGGER.debug("Updating the ScenarioDefinition with the name '{}'.", scenarioDefinition.getScenarioName());
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		
		String sdName 			= scenarioDefinition.getScenarioName();
		ScenarioDefinition sd 	= ServiceStorageModul.loadScenarioDefinition(sdName, usertoken);
		
		if (sd == null) {
			LOGGER.info("Cannot match ScenarioDefinition name to a given one!");
			return Response.status(Status.CONFLICT).entity("Cannot match ScenarioDefinition name to a given one!").build();
		}
		
		sd = scenarioDefinition;

		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}
		
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
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_SCENARIO_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioAsXML(@QueryParam(TOKEN) String usertoken,
									 @PathParam(NAME) String scenarioname) {
		
		if (usertoken == null || scenarioname == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition definition = ServiceStorageModul.loadScenarioDefinition(scenarioname, usertoken);

		if (definition == null) {
			LOGGER.info("No ScenarioDefinition with given name in database!");
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name in database!").build();
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
	public Response getScenarioInstance(@HeaderParam("host") String host,
										@QueryParam(TOKEN) String usertoken,
										@QueryParam(ServiceConfiguration.SVCP_SCENARIO_NAME) String name,
										@QueryParam(ServiceConfiguration.SVCP_SCENARIO_URL) String url) {
			
		if (host == null || usertoken == null || name == null || url == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
	
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			ScenarioInstance tmpSI = AccountPersistenceProvider.createPersistenceProvider(usertoken).loadScenarioInstance(name, url);
			decoratedExperimentSeriesRuns(tmpSI, u.getAccountID(), host);
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
	public Response getScenarioInstances(@HeaderParam("host") String host,
										 @QueryParam(TOKEN) String usertoken,
										 @QueryParam(ServiceConfiguration.SVCP_SCENARIO_NAME) String name) {
		
		if (host == null || usertoken == null || name == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			List<ScenarioInstance> listSI = AccountPersistenceProvider.createPersistenceProvider(usertoken).loadScenarioInstances(name);
			
			for (ScenarioInstance si : listSI) {
				decoratedExperimentSeriesRuns(si, u.getAccountID(), host);
			}
			
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
	@Path("{" + NAME + "}/" + ServiceConfiguration.SVC_SCENARIO_DEFINITON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioDefinition(@QueryParam(TOKEN) String usertoken,
										  @PathParam(NAME) String scenarioName) {
			
		if (usertoken == null || scenarioName == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name in database!");
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name in database!").build();
		}
		
		return Response.ok(sd).build();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// HELPER /////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Archiving old scenario results from the given {@code ScenarioInstance} into the
	 * account database.
	 * 
	 * @param u the user who wants to archive
	 * @param 	scenarioInstance the scenario to save
	 */
	private void archiveOldResults(Users u, ScenarioInstance scenarioInstance) {
		
		ScenarioDefinitionWriter writer = new ScenarioDefinitionWriter(u.getToken());
		
		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(u.getToken());
		
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
	
	/**
	 * Converts all the {@link ExperimentSeriesRun}s in the given {@link ScenarioInstance}
	 * into {@link ExperimentSeriesRunDecorator}s.<br />
	 * The account ID is required to pass them to the {@link ExperimentSeriesRunDecorator} to have
	 * access to the Service Layer afterwards to fetch the results. See comments in the class for more
	 * information.<br />
	 * The passed hostURI is analysed, wheather it's "http://myserviceurl.de:8080" or <br />
	 * "http://myserviceurl.de".
	 * 
	 * @param scenarioInstance	the {@link ScenarioInstance}
	 * @param accountID			the account ID of the caller
	 * @param host				the host of the service layer
	 */
	private void decoratedExperimentSeriesRuns(ScenarioInstance scenarioInstance, long accountID, String hostURI) {
		if (scenarioInstance == null) return;
			
    	for(ExperimentSeries es : scenarioInstance.getExperimentSeriesList()) {
    		
    		// need these lists to avoid ConcurrentModificationException in the next for loop
    		List<ExperimentSeriesRun> esrToRemove 	= new ArrayList<ExperimentSeriesRun>();
    		List<ExperimentSeriesRun> esrToAdd 		= new ArrayList<ExperimentSeriesRun>();
    		
    		for(ExperimentSeriesRun esr : es.getExperimentSeriesRuns()) {
    			
    			ExperimentSeriesRunDecorator esrd = new ExperimentSeriesRunDecorator(esr, accountID, hostURI);
    			
    			esrToAdd.add(esrd);
    			esrToRemove.add(esr);
    		}
    			
    		es.getExperimentSeriesRuns().removeAll(esrToRemove);
    		es.getExperimentSeriesRuns().addAll(esrToAdd);
    	}
		
	}
}
