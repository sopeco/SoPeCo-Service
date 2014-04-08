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
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.ServiceStorageModul;
import org.sopeco.service.helper.SimpleEntityFactory;
import org.sopeco.service.persistence.AccountPersistenceProvider;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.Users;


@Path(ServiceConfiguration.SVC_EXPERIMENTSERIES)
public class ExperimentSeriesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSeriesService.class);

	/**
	 * As the scenario name needs to be always passed, a shortener is introduced.
	 */
	private static final String SCENARIONAME = ServiceConfiguration.SVC_EXPERIMENTSERIES_SCENARIONAME;

	/**
	 * As the measurment specification needs to be always passed, a shortener is introduced.
	 */
	private static final String MEASURMENTSPECNAME = ServiceConfiguration.SVC_EXPERIMENTSERIES_MEASUREMENTSPECNAME;
	
	/**
	 * The token is always needed at REST interfaces. A shortener is therefor introduced.
	 */
	private static final String TOKEN = ServiceConfiguration.SVCP_EXPERIMENTSERIES_TOKEN;
	
	
	@GET
	@Path("{" + SCENARIONAME + "}/" + MEASURMENTSPECNAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllExperimentSeries(@PathParam(SCENARIONAME) String scenarioName,
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
		
		ScenarioDefinition sd = ServiceStorageModul.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			return Response.status(Status.CONFLICT).entity("Scenario with given name does not exist.").build();
		}
		
		return null;
		
	}
}
