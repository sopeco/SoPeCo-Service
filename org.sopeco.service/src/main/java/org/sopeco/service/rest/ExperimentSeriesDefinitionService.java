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

import java.util.List;
import java.util.Set;

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
import org.sopeco.persistence.entities.definition.ConstantValueAssignment;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ExperimentTerminationCondition;
import org.sopeco.persistence.entities.definition.ExplorationStrategy;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ParameterValueAssignment;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.ServiceStorageModul;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.Users;

/**
 * The service provides methods to modify the {@link ExperimentSeriesDefinition} in a {@link MeasurementSpecification}
 * of a {@link ScenarioDefinition}.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_ESD)
public class ExperimentSeriesDefinitionService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSeriesDefinitionService.class);

	/**
	 * As the scenario name for a {@link ScenarioDefinition} needs to be always passed; a shortener is introduced.
	 */
	private static final String SCENARIONAME = ServiceConfiguration.SVC_ESD_SCENARIONAME;

	/**
	 * As the {@link MeasurementSpecification} needs to be always passed; a shortener is introduced.
	 */
	private static final String MEASURMENTSPECNAME = ServiceConfiguration.SVC_ESD_MEASUREMENTSPECNAME;
	
	/**
	 * The token is always needed at REST interfaces. A shortener is therefor introduced.
	 */
	private static final String TOKEN = ServiceConfiguration.SVCP_ESD_TOKEN;
	
	/**
	 * The token is always needed at REST interfaces. A shortener is therefor introduced.
	 */
	private static final String EXPSERDEFNAME = ServiceConfiguration.SVCP_ESD_EXPSERDEFNAME;
	

	/**
	 * Renames the {@link ExperimentSeriesDefinition} given in the URI with the name passed as parameter.
	 * No new {@link ExperimentSeriesDefinition} will be created.
	 * 
	 * @param scenarioName		the name of the {@link ScenarioDefinition}
	 * @param measSpecName		the name of the {@link MeasurementSpecification}
	 * @param expSerDefName		the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken			the user identification
	 * @param newExpSerDefName 	the new name of the {@link ExperimentSeriesDefinition}
	 * @return					{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@POST
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameExperimentSeriesDefinition(@PathParam(SCENARIONAME) String scenarioName,
										   		  	 @PathParam(MEASURMENTSPECNAME) String measSpecName,
											   		 @PathParam(EXPSERDEFNAME) String expSerDefName,
										   		  	 @QueryParam(ServiceConfiguration.SVCP_ESD_NEWEXPSERDEFNAME) String newExpSerDefName,
										   		     @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || newExpSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ScenarioDefinition sd 			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, newExpSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition or MeasurementSpecification or ExperimentSeriesDefinition with given name does not exist.").build();
		}
		
		esd.setName(newExpSerDefName);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns all the {@link ExperimentSeriesDefinition} in the {@link MeasurementSpecification} of
	 * the {@link ScenarioDefinition}.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 						OK with {@link List} of {@link ExperimentSeriesDefinition} as {@link Entity}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllExperimentSeriesDefinition(@PathParam(SCENARIONAME) String scenarioName,
										   			 @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   			 @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		MeasurementSpecification ms = ServiceStorageModul.loadMeasurementSpecification(scenarioName, measSpecName, usertoken);
		
		if (ms == null) {
			return Response.status(Status.CONFLICT).entity("Scenario or MeasurementSpecification with given name does not exist .").build();
		}
		
		return Response.ok(ms.getExperimentSeriesDefinitions()).build();
	}
	
	/**
	 * Returns the {@link ExperimentSeriesDefinition} with the name in the URI.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param expSerDefName	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 						OK with {@link List} of {@link ExperimentSeriesDefinition} as {@link Entity}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExperimentSeriesDefinition(@PathParam(SCENARIONAME) String scenarioName,
										   		  @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   		  @PathParam(EXPSERDEFNAME) String expSerDefName,
										   		  @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		MeasurementSpecification ms = ServiceStorageModul.loadMeasurementSpecification(scenarioName, measSpecName, usertoken);
		
		if (ms == null) {
			return Response.status(Status.CONFLICT).entity("ScenarioDefitnion or MeasurementSpecification with given name does not exist.").build();
		}
		
		List<ExperimentSeriesDefinition> listESD = ms.getExperimentSeriesDefinitions();
		
		for (ExperimentSeriesDefinition esd : listESD) {
			
			if (esd.getName().equals(expSerDefName)) {
				return Response.ok(esd).build();
			}
			
		}

		return Response.status(Status.CONFLICT).entity("No ExperimentSeries in the MeasurementSpecification with the given name.").build();
	}
	
	/**
	 * Adds a the passed {@link ExperimentSeriesDefinition} to the {@link MeasurementSpecification}. If a {@link ExperimentSeriesDefinition}
	 * with the given name already exists, it is overwritten.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param expSerDefName	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addExperimentSeriesDefinition(@PathParam(SCENARIONAME) String scenarioName,
										   		  @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   		  @QueryParam(TOKEN) String usertoken,
										   		  ExperimentSeriesDefinition expSerDef) {
		
		if (scenarioName == null || measSpecName == null || expSerDef == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ScenarioDefinition sd 		= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		MeasurementSpecification ms = ServiceStorageModul.loadMeasurementSpecification(scenarioName, measSpecName, usertoken);
		
		if (ms == null) {
			return Response.status(Status.CONFLICT).entity("Scenario with given name does not exist.").build();
		}
		
		List<ExperimentSeriesDefinition> listESD = ms.getExperimentSeriesDefinitions();
		
		// search, if an ESD with the given name already exists
		boolean found = false;
		for (ExperimentSeriesDefinition esd : listESD) {
			
			if (esd.getName().equals(expSerDef.getName())) {
				found = true;
				esd = expSerDef;
				return Response.ok().build();
			}
			
		}

		// when there is no ESD with the same name, it can be safely added
		if (!found) {
			listESD.add(expSerDef);
		}

		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}

	/**
	 * Deletes the {@link ExperimentSeriesDefinition} with the given name.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param expSerDefName	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeExperimentSeriesDefinition(@PathParam(SCENARIONAME) String scenarioName,
										   		     @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   		     @PathParam(EXPSERDEFNAME) String expSerDefName,
										   		     @QueryParam(TOKEN) String usertoken) {
		  
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ScenarioDefinition sd 		= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		MeasurementSpecification ms = ServiceStorageModul.loadMeasurementSpecification(scenarioName, measSpecName, usertoken);
		
		if (ms == null) {
			return Response.status(Status.CONFLICT).entity("ScenarioDefitnion or MeasurementSpecification with given name does not exist.").build();
		}
		
		List<ExperimentSeriesDefinition> listESD = ms.getExperimentSeriesDefinitions();
		
		ExperimentSeriesDefinition toRemove = null;
		for (ExperimentSeriesDefinition esd : listESD) {
			
			if (esd.getName().equals(expSerDefName)) {
				toRemove = esd;
				break;
			}
			
		}
		
		if (toRemove != null) {
			listESD.remove(toRemove);
		}

		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns the {@link ExplorationStrategy} of the {@link ExperimentSeriesDefinition}, which can be identified via the given
	 * parameters.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param expSerDefName	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 						OK with {@link ExplorationStrategy} as {@link Entity}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_EXPLORATIONSTRATEGY)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExplorationStrategy(@PathParam(SCENARIONAME) String scenarioName,
										   @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   @PathParam(EXPSERDEFNAME) String expSerDefName,
										   @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ExperimentSeriesDefinition esd = ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		return Response.ok(esd.getExplorationStrategy()).build();
	}
	
	/**
	 * Adds the {@link ExplorationStrategy} of the {@link ExperimentSeriesDefinition}, which can be identified via the given
	 * parameters. If an {@link ExplorationStrategy} already exists, it's overwritten.
	 * 
	 * @param scenarioName			the name of the {@link ScenarioDefinition}
	 * @param measSpecName			the name of the {@link MeasurementSpecification}
	 * @param expSerDefName			the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @param explorationStrategy	the {@link ExplorationStrategy}
	 * @return						{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_EXPLORATIONSTRATEGY)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addExplorationStrategy(@PathParam(SCENARIONAME) String scenarioName,
										   @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   @PathParam(EXPSERDEFNAME) String expSerDefName,
										   @QueryParam(TOKEN) String usertoken,
										   ExplorationStrategy explorationStrategy) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || explorationStrategy == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd 			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		esd.setExplorationStrategy(explorationStrategy);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Removes the {@link ExplorationStrategy} from the {@link ExperimentSeriesDefinition}.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param expSerDefName	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_EXPLORATIONSTRATEGY)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeExplorationStrategy(@PathParam(SCENARIONAME) String scenarioName,
										   @PathParam(MEASURMENTSPECNAME) String measSpecName,
										   @PathParam(EXPSERDEFNAME) String expSerDefName,
										   @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd 			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		esd.setExplorationStrategy(null);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns the {@link Set} of {@link ExperimentTerminationCondition} of the {@link ExperimentSeriesDefinition}, which can be
	 * identified via the given parameters.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param expSerDefName	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 						OK with {@link Set} of {@link ExperimentTerminationCondition} as {@link Entity}		
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_TERMINATIONCONDITIONS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTerminationCondition(@PathParam(SCENARIONAME) String scenarioName,
										    @PathParam(MEASURMENTSPECNAME) String measSpecName,
										    @PathParam(EXPSERDEFNAME) String expSerDefName,
										    @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ExperimentSeriesDefinition esd = ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		return Response.ok(esd.getTerminationConditions()).build();
	}
	
	/**
	 * Adds the given {@link ExperimentTerminationCondition} to the {@link Set} of {@link ExperimentSeriesDefinition}, which can be
	 * identified via the given parameters. If one with the same name already exists, it's overwritten.
	 * 
	 * @param scenarioName			the name of the {@link ScenarioDefinition}
	 * @param measSpecName			the name of the {@link MeasurementSpecification}
	 * @param expSerDefName			the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @param terminationCondition	the {@link ExperimentTerminationCondition} to set
	 * @return						{@link Response} OK, UNAUTHORIZED or CONFLICT	
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_TERMINATIONCONDITIONS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setTerminationCondition(@PathParam(SCENARIONAME) String scenarioName,
										    @PathParam(MEASURMENTSPECNAME) String measSpecName,
										    @PathParam(EXPSERDEFNAME) String expSerDefName,
										    @QueryParam(TOKEN) String usertoken,
										    ExperimentTerminationCondition terminationCondition) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || terminationCondition == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd 			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		// now actually add the ExperimentTerminationCondition
		boolean found = false;
		for (ExperimentTerminationCondition etc : esd.getTerminationConditions()) {
			
			if (etc.getName().equals(terminationCondition.getName())) {
				etc = terminationCondition;
				found = true;
				break;
			}
			
		}
		
		if (!found) {
			esd.addTerminationCondition(terminationCondition);
		}
		
		// save changes in database
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Removes the given {@link ExperimentTerminationCondition} with the from the {@link Set} of {@link ExperimentSeriesDefinition}, which can be
	 * identified via the given parameters. Actually only the name of the {@link ExperimentTerminationCondition} is checked here. So you can pass
	 * a {@link ExperimentTerminationCondition}, which only has a name set.
	 * 
	 * @param scenarioName			the name of the {@link ScenarioDefinition}
	 * @param measSpecName			the name of the {@link MeasurementSpecification}
	 * @param expSerDefName			the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @param terminationCondition	the {@link ExperimentTerminationCondition} to set
	 * @return						{@link Response} OK, UNAUTHORIZED or CONFLICT	
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_TERMINATIONCONDITIONS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeTerminationCondition(@PathParam(SCENARIONAME) String scenarioName,
										       @PathParam(MEASURMENTSPECNAME) String measSpecName,
										       @PathParam(EXPSERDEFNAME) String expSerDefName,
										       @QueryParam(TOKEN) String usertoken,
										       ExperimentTerminationCondition terminationCondition) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || terminationCondition == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd 			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		ExperimentTerminationCondition toRemove = null;
		for (ExperimentTerminationCondition etc : esd.getTerminationConditions()) {
			if (etc.getName().equals(terminationCondition.getName())) {
				toRemove = etc;
				break;
			}
		}
		
		// avoid concurrent modification of the traveresed set above
		if (toRemove != null) {
			esd.getTerminationConditions().remove(toRemove);
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns the experiment assignments ({@link List}<{@link ParameterValueAssignment}>) of the {@link ExperimentSeriesDefinition}.
	 * 
	 * @param scenarioName		the name of the {@link ScenarioDefinition}
	 * @param measSpecName		the name of the {@link MeasurementSpecification}
	 * @param expSerDefName		the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken			the user identification
	 * @return					{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 							OK with {@link List}<{@link ParameterValueAssignment}> as {@link Entity}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_EXPERIMENTASSIGNMENTS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExperimentAssignment(@PathParam(SCENARIONAME) String scenarioName,
										    @PathParam(MEASURMENTSPECNAME) String measSpecName,
										    @PathParam(EXPSERDEFNAME) String expSerDefName,
										    @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ExperimentSeriesDefinition esd = ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		return Response.ok(esd.getExperimentAssignments()).build();
	}
	
	/**
	 * Adds a {@link ParameterValueAssignment} to the experiment assignments of the {@link ExperimentSeriesDefinition}.
	 * If there is already a {@link ParameterValueAssignment} with the same parameter name and in the same namespace,
	 * it is overwritten.
	 * 
	 * @param scenarioName				the name of the {@link ScenarioDefinition}
	 * @param measSpecName				the name of the {@link MeasurementSpecification}
	 * @param expSerDefName				the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken					the user identification
	 * @param parameterValueAssignment	the {@link ParameterValueAssignment} to add to the experiment assignments
	 * @return							{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_EXPERIMENTASSIGNMENTS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addExperimentAssignment(@PathParam(SCENARIONAME) String scenarioName,
										    @PathParam(MEASURMENTSPECNAME) String measSpecName,
										    @PathParam(EXPSERDEFNAME) String expSerDefName,
										    @QueryParam(TOKEN) String usertoken,
										    ParameterValueAssignment parameterValueAssignment ) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || parameterValueAssignment == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}

		boolean found = false;
		for (ParameterValueAssignment pva : esd.getExperimentAssignments()) {
			
			if (pva.getParameter().getName().equals(parameterValueAssignment.getParameter().getFullName())
					&& pva.getParameter().getNamespace().getName().equals(parameterValueAssignment.getParameter().getNamespace().getFullName())) {
				
				pva 	= parameterValueAssignment;
				found 	= true;
				break;
			}
			
		}
		
		if (!found) {
			esd.getExperimentAssignments().add(parameterValueAssignment);
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok(esd.getExperimentAssignments()).build();
	}
	
	/**
	 * Removes the experiment assignment, which is given as a {@link ParameterValueAssignment}. The {@link ParameterValueAssignment} is removed
	 * from the {@link ExperimentSeriesDefinition} which can be identified via the given parameter.<br />
	 * To remove and existing {@link ParameterValueAssignment}, the name of the parameter and the full parameter namespace must match with the
	 * given {@link ParameterValueAssignment}. (Other fields are not touched.)
	 * 
	 * @param scenarioName				the name of the {@link ScenarioDefinition}
	 * @param measSpecName				the name of the {@link MeasurementSpecification}
	 * @param expSerDefName				the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken					the user identification
	 * @param parameterValueAssignment	the {@link ParameterValueAssignment} to delete
	 * @return							{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_EXPERIMENTASSIGNMENTS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeExperimentAssignment(@PathParam(SCENARIONAME) String scenarioName,
										       @PathParam(MEASURMENTSPECNAME) String measSpecName,
										       @PathParam(EXPSERDEFNAME) String expSerDefName,
										       @QueryParam(TOKEN) String usertoken,
										       ParameterValueAssignment parameterValueAssignment ) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || parameterValueAssignment == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}

		ParameterValueAssignment toRemove = null;
		for (ParameterValueAssignment pva : esd.getExperimentAssignments()) {
			
			if (pva.getParameter().getName().equals(parameterValueAssignment.getParameter().getFullName())
					&& pva.getParameter().getNamespace().getName().equals(parameterValueAssignment.getParameter().getNamespace().getFullName())) {
				
				toRemove = pva;
				break;
			}
			
		}
		
		if (toRemove != null) {
			esd.getExperimentAssignments().remove(toRemove);
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok(esd.getExperimentAssignments()).build();
	}
	
	/**
	 * Returns the preparation assignments ({@link List}<{@link ConstantValueAssignment}>) of the {@link ExperimentSeriesDefinition}.
	 * 
	 * @param scenarioName		the name of the {@link ScenarioDefinition}
	 * @param measSpecName		the name of the {@link MeasurementSpecification}
	 * @param expSerDefName		the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken			the user identification
	 * @return					{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 							OK with {@link List}<{@link ConstantValueAssignment}> as {@link Entity}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_PREPARATIONASSIGNMENTS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPreparationAssignment(@PathParam(SCENARIONAME) String scenarioName,
										     @PathParam(MEASURMENTSPECNAME) String measSpecName,
										     @PathParam(EXPSERDEFNAME) String expSerDefName,
										     @QueryParam(TOKEN) String usertoken) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ExperimentSeriesDefinition esd = ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}
		
		return Response.ok(esd.getPreperationAssignments()).build();
	}
	
	/**
	 * Adds a {@link ConstantValueAssignment} to the preparation assignments of the {@link ExperimentSeriesDefinition}.
	 * If there is already a {@link ConstantValueAssignment} with the same parameter name and in the same namespace,
	 * it is overwritten.
	 * 
	 * @param scenarioName				the name of the {@link ScenarioDefinition}
	 * @param measSpecName				the name of the {@link MeasurementSpecification}
	 * @param expSerDefName				the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken					the user identification
	 * @param constantValueAssignment	the {@link ConstantValueAssignment} to add to the experiment assignments
	 * @return							{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_PREPARATIONASSIGNMENTS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPreparationAssignment(@PathParam(SCENARIONAME) String scenarioName,
										     @PathParam(MEASURMENTSPECNAME) String measSpecName,
										     @PathParam(EXPSERDEFNAME) String expSerDefName,
										     @QueryParam(TOKEN) String usertoken,
										     ConstantValueAssignment constantValueAssignment ) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || constantValueAssignment == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}

		boolean found = false;
		for (ConstantValueAssignment cva : esd.getPreperationAssignments()) {
			
			if (cva.getValue().equals(constantValueAssignment.getValue())
				&& cva.getParameter().getFullName().equals(constantValueAssignment.getParameter().getFullName())) {
				
				cva 	= constantValueAssignment;
				found 	= true;
				break;
				
			}
			
		}
		
		if (!found) {
			esd.getExperimentAssignments().add(constantValueAssignment);
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok(esd.getExperimentAssignments()).build();
	}
	
	/**
	 * Removes the preparation assignment, which is given as a {@link ConstantValueAssignment}. The {@link ConstantValueAssignment}
	 * is removed from the {@link ExperimentSeriesDefinition} which can be identified via the given parameter.<br />
	 * To remove and existing {@link ConstantValueAssignment}, the value of the parameter and the full parameter name must match
	 * with the given {@link ConstantValueAssignment}. (Other fields are not touched.)
	 * 
	 * @param scenarioName				the name of the {@link ScenarioDefinition}
	 * @param measSpecName				the name of the {@link MeasurementSpecification}
	 * @param expSerDefName				the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken					the user identification
	 * @param constantValueAssignment	the {@link ConstantValueAssignment} to delete
	 * @return							{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + MEASURMENTSPECNAME + "}/{" + EXPSERDEFNAME + "}/" + ServiceConfiguration.SVC_ESD_PREPARATIONASSIGNMENTS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removePreparationAssignment(@PathParam(SCENARIONAME) String scenarioName,
										        @PathParam(MEASURMENTSPECNAME) String measSpecName,
										        @PathParam(EXPSERDEFNAME) String expSerDefName,
										        @QueryParam(TOKEN) String usertoken,
										        ConstantValueAssignment constantValueAssignment ) {
		
		if (scenarioName == null || measSpecName == null || expSerDefName == null || usertoken == null || constantValueAssignment == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd			= ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		ExperimentSeriesDefinition esd 	= ServiceStorageModul.loadExperimentSeriesDefinition(scenarioName, measSpecName, expSerDefName, usertoken);
		
		if (esd == null) {
			return Response.status(Status.CONFLICT).entity("ExperimentSeriesDefinition cannot be found in database. Check the name of the ScenarioDefinition and MeasurementSpecification.").build();
		}

		ConstantValueAssignment toRemove = null;
		for (ConstantValueAssignment cva : esd.getPreperationAssignments()) {
			
			if (cva.getValue().equals(constantValueAssignment.getValue())
				&& cva.getParameter().getFullName().equals(constantValueAssignment.getParameter().getFullName())) {
				
				toRemove = cva;
				break;
				
			}
			
		}
		
		if (toRemove != null) {
			esd.getExperimentAssignments().remove(toRemove);
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok(esd.getExperimentAssignments()).build();
	}
	
}
