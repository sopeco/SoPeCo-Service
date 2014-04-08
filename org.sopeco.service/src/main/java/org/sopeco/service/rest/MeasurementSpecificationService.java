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
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.definition.ConstantValueAssignment;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.ServiceStorageModul;
import org.sopeco.service.helper.SimpleEntityFactory;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.AccountPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;

/**
 * This class handles the meeasurement specifications (MS). 
 * A {@link ScenarioDefinition} does have a list of {@link MeasurementSpecification}s and a
 * {@link MeasurementEnvironmentDefinition}.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_MEASUREMENTSPEC)
public class MeasurementSpecificationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementSpecificationService.class);

	/**
	 * As the scenario definition must be always loaded before the MeasurementSpecification can 
	 * be changed, the variable is often used. It's just a shortener.
	 */
	private static final String SCENARIONAME = ServiceConfiguration.SVCP_MEASUREMENTSPEC_SCENARIONAME;

	/**
	 * The token is always needed at REST interfaces. A shortener is therefor introduced.
	 */
	private static final String TOKEN = ServiceConfiguration.SVCP_MEASUREMENTSPEC_TOKEN;
	
	/**
	 * Lists all the current {@link MeasurementSpecification}s for the user with the given token.
	 * 
	 * @param usertoken 	the user identification
	 * @param scenarioName	the scenario name
	 * @return 				{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 						OK with {@link Entity} List<String> of all {@link MeasurementSpecification}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/" + ServiceConfiguration.SVC_MEASUREMENTSPEC_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeasurementSpecificationNames(@QueryParam(TOKEN) String usertoken,
														@PathParam(SCENARIONAME) String scenarioName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection to account database found.");
			return Response.status(Status.UNAUTHORIZED).build();
		} 
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioName);
			
			if (scenarioDefinition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return Response.status(Status.CONFLICT).entity("ScenarioDefinition is invalid.").build();
			}
			
			List<String> returnList = new ArrayList<String>();
			for (MeasurementSpecification ms : scenarioDefinition.getMeasurementSpecifications()) {
				returnList.add(ms.getName());
			}
			
			return Response.ok(returnList).build();
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Cannot fetch ScenarioDefinition from account database.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot fetch ScenarioDefinition from account database.").build();
		}
		
	}
	
	/**
	 * List all the {@link MeasurementSpecification} as it. So the object returnde is a list of MS objects.
	 * 
	 * @param usertoken		the user identification
	 * @param scenarioName	the scenario name
	 * @return 				{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 						OK with {@link Entity} List<{@link MeasurementSpecification}>
	 */
	@GET
	@Path("{" + SCENARIONAME + "}/" + ServiceConfiguration.SVC_MEASUREMENTSPEC_LISTSPECS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeasurementSpecifications(@QueryParam(TOKEN) String usertoken,
													@PathParam(SCENARIONAME) String scenarioName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection to account database found.");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			
			ScenarioDefinition scenarioDefinition = dbCon.loadScenarioDefinition(scenarioName);
			
			if (scenarioDefinition == null) {
				LOGGER.warn("ScenarioDefinition is invalid.");
				return Response.status(Status.CONFLICT).entity("ScenarioDefinition is invalid.").build();
			}
			
			List<MeasurementSpecification> returnList = new ArrayList<MeasurementSpecification>();
			for (MeasurementSpecification ms : scenarioDefinition.getMeasurementSpecifications()) {
				returnList.add(ms);
			}
			
			return Response.ok(returnList).build();
			
		} catch (DataNotFoundException e) {
			LOGGER.warn("Cannot fetch ScenarioDefinition from account database.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot fetch ScenarioDefinition from account database.").build();
		}
		
	}
	
	/**
	 * Creates a new {@link MeasurementSpecification} (MS).<br />
	 * Return false, if a MS with the given name already exists or the addition failed.
	 * 
	 * @param usertoken 		the user identification
	 * @param specificationName the name for the new {@link MeasurementSpecification}
	 * @return 					{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR
	 */
	@POST
	@Path("{" + SCENARIONAME + "}/" + ServiceConfiguration.SVC_MEASUREMENTSPEC_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSpecification(@QueryParam(TOKEN) String usertoken,
									    @QueryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME) String specificationName,
									    @PathParam(SCENARIONAME) String scenarioName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (existSpecification(specificationName, scenarioName, usertoken)) {
			LOGGER.warn("Specification with the name '{}' already exists.", specificationName);
			return Response.status(Status.CONFLICT).entity("Specification with the given name already exists.").build();
		}

		MeasurementSpecification ms = SimpleEntityFactory.createMeasurementSpecification(specificationName);
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		sd.getMeasurementSpecifications().add(ms);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);

		return Response.ok().build();
	}
	
	/**
	 * Renames the given  {@link MeasurementSpecification} (MS) via name with the new given name.
	 * Changes the name of the user's selected MS, too.
	 * 
	 * @param usertoken the user identification
	 * @param specname 	the new MS name
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}/{" + ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME + "}/" + ServiceConfiguration.SVC_MEASUREMENTSPEC_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameWorkingSpecification(@QueryParam(TOKEN) String usertoken,
											   @QueryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_SPECNAME) String newMeasurementSpecificationName,
											   @PathParam(SCENARIONAME) String scenarioName,
											   @PathParam(ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME) String measurementSpecificationName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (existSpecification(newMeasurementSpecificationName, scenarioName, usertoken)) {
			LOGGER.warn("Can't rename, because specification with the name '{}' already exists.", newMeasurementSpecificationName);
			return Response.status(Status.CONFLICT).entity("Can't rename, because specification with the given name already exists.").build();
		}
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		MeasurementSpecification ms = sd.getMeasurementSpecification(measurementSpecificationName);
		
		if (ms == null) {
			LOGGER.warn("No MeasurementSpecification with the name '{}' exists.", measurementSpecificationName);
			return Response.status(Status.CONFLICT).entity("No MeasurementSpecification with the given name exists.").build();	
		}
		
		ms.setName(newMeasurementSpecificationName);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);
		
		return Response.ok().build();
	}
	
	/**
	 * Removes the {@link MeasurementSpecification} with the given name.
	 * 
	 * @param usertoken the user identification
	 * @param specname 	the new MS name
	 * @return 			{@link Response} OK, UNAUTHORIZED, CONFLICT or INTERNAL_SERVER_ERROR<br />
	 * 					CONFLICT can occur, when the {@link MeasurementSpecification} with the passed
	 * 					name has been selected
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeWorkingSpecification(@QueryParam(TOKEN) String usertoken,
											   @PathParam(SCENARIONAME) String scenarioName,
											   @PathParam(ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME) String measurementSpecificationName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (!existSpecification(measurementSpecificationName, scenarioName, usertoken)) {
			LOGGER.warn("Can't delete MeasurementSpecification, because it doesn't exist.");
			return Response.status(Status.CONFLICT).entity("Can't delete MeasurementSpecification, because it doesn't exist.").build();
		}
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		MeasurementSpecification ms = sd.getMeasurementSpecification(measurementSpecificationName);
		
		if (ms == null) {
			LOGGER.warn("No MeasurementSpecification with the name '{}' exists.", measurementSpecificationName);
			return Response.status(Status.CONFLICT).entity("No MeasurementSpecification with the given name exists.").build();	
		}
		
		sd.getMeasurementSpecifications().remove(ms);
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);

		return Response.ok().build();
	}
	
	/**
	 * Adds or updates an initial assignments in the {@link MeasurementSpecification}. The initial assignment is an 
	 * {@link ConstantValueAssignment} in SoPeCo and has a {@link ParameterDefinition} and a value, which must be both provided here.
	 * The initial assignment is configured to every {@link ExperimentSeries} in this {@link MeasurementSpecification}.
	 * 
	 * @param specificationName	the name of the {@link MeasurementSpecification} to change
	 * @param scenarioName		the {@link ScenarioDefinition} name to update
	 * @param usertoken			the user identification
	 * @param param				the {@link ParameterDefinition}, which is one part of the {@link ConstantValueAssignment}
	 * @param parameterValue	the value, which is the second part of the {@link ConstantValueAssignment}
	 * @return					{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@POST
	@Path("{" + SCENARIONAME + "}/{" + ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME + "}" + ServiceConfiguration.SVCP_MEASUREMENTSPEC_INITIALASSIGNMENT)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addInitialAssignments(@PathParam(ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME) String specificationName,
		      							  @PathParam(SCENARIONAME) String scenarioName,
		      							  @QueryParam(TOKEN) String usertoken,
									      @QueryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_PARAMETERDEF) ParameterDefinition param,
									      @QueryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_PARAMETERVAL) String parameterValue) {

		if (usertoken == null || specificationName == null || scenarioName == null || param == null || parameterValue == null) {
			return Response.status(Status.CONFLICT).entity("One or more values are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		MeasurementSpecification ms = sd.getMeasurementSpecification(specificationName);
		
		if (ms == null) {
			LOGGER.warn("MeasurementSpecification with the name '{}' doesn't exist.", specificationName);
			return Response.status(Status.CONFLICT).entity("MeasurementSpecification with the given name doesn't exist.").build();	
		}
		
		List<ConstantValueAssignment> listCVA = ms.getInitializationAssignemts();
		
		// check if the assignment is already there
		boolean found = false;
		for (ConstantValueAssignment cva : listCVA) {
			
			if (cva.getParameter().equals(param)) {
				cva.setValue(parameterValue);
				found = true;
				break;
			}
			
		}
		
		if (!found) {
			ConstantValueAssignment cva = new ConstantValueAssignment();
			cva.setParameter(param);
			cva.setValue(parameterValue);
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);

		return Response.ok().build();
	}
	
	/**
	 * Removes an intiail assignment of a {@link MeasurementSpecification}.
	 * 
	 * @param specificationName	the name of the {@link MeasurementSpecification} to change
	 * @param scenarioName		the {@link ScenarioDefinition} name to update
	 * @param usertoken			the user identification
	 * @param param				the {@link ParameterDefinition} to delete out of the initial assignments
	 * @return					{@link Response} OK, UNAUTHORIZED or CONFLICT
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}/{" + ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME + "}" + ServiceConfiguration.SVCP_MEASUREMENTSPEC_INITIALASSIGNMENT)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeInitialAssignments(@PathParam(ServiceConfiguration.SVC_MEASUREMENTSPEC_NAME) String specificationName,
		      							  	 @PathParam(SCENARIONAME) String scenarioName,
		      							  	 @QueryParam(TOKEN) String usertoken,
		      							  	 @QueryParam(ServiceConfiguration.SVCP_MEASUREMENTSPEC_PARAMETERDEF) ParameterDefinition param) {

		if (usertoken == null || specificationName == null || scenarioName == null || param == null) {
			return Response.status(Status.CONFLICT).entity("One or more values are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.warn("ScenarioDefinition with the name '{}' doesn't exists.", scenarioName);
			return Response.status(Status.CONFLICT).entity("ScenarioDefinition with the given name doesn't exists.").build();	
		}
		
		MeasurementSpecification ms = sd.getMeasurementSpecification(specificationName);
		
		if (ms == null) {
			LOGGER.warn("MeasurementSpecification with the name '{}' doesn't exist.", specificationName);
			return Response.status(Status.CONFLICT).entity("MeasurementSpecification with the given name doesn't exist.").build();	
		}
		
		List<ConstantValueAssignment> listCVA = ms.getInitializationAssignemts();

		for (ConstantValueAssignment cva : listCVA) {
			
			if (cva.getParameter().equals(param)) {
				listCVA.remove(cva);
				break;
			}
			
		}
		
		ServiceStorageModul.storeScenarioDefition(usertoken, sd);

		return Response.ok().build();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// HELPER /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns whether a {@link MeasurementSpecification} with the given name exists.
	 * 
	 * @param specification MS name
	 * @param scenarioName	the name of the scenario
	 * @param token			the user identification
	 * @return 				true, if MS with the given name exists
	 */
	private boolean existSpecification(String specification, String scenarioName, String token) {

		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(token);
		
		ScenarioDefinition sd = null;
		
		try {
			
			sd = dbCon.loadScenarioDefinition(scenarioName);
			
		} catch (DataNotFoundException e) {
			
			LOGGER.info("Cannot find a ScenarioDefition with the given name in the database.");
			
		}
		
		if (sd != null) {
		
			for (MeasurementSpecification ms : sd.getMeasurementSpecifications()) {
				
				if (specification.equals(ms.getName())) {
					return true;
				}
				
			}
		
		}
		
		return false;
	}
}
