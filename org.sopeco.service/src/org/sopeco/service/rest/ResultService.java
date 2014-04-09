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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.AccountPersistenceProvider;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.rest.exchange.ExperimentSeriesRunDecorator;

/**
 * The {@link ResultService} class provides RESTful services to access results in SoPeCo.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_RESULT)
public class ResultService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultService.class.getName());

	/**
	 * Returns {@link DataSetAggregated} which matches to the given dataSetID for an Account (identified via
	 * the given accountID). As this method can be called anytime after the client has once recevied the
	 * {@link ScenarioInstance}s after an experiment run, this method cannot use authentification via tokens. A
	 * token could be already expired. The problem is, that this method is most times called via an injection
	 * of an {@link ExperimentSeriesRunDecorator} into the {@link ScenarioInstance}s the client receives. The
	 * {@link ExperimentSeriesRunDecorator} will call this method. If authentification is used, the 
	 * {@link ExperimentSeriesRunDecorator} would need more information about logging in etc. This is overhead
	 * and therefor the log in check disabled here.<br />
	 * But to get the correct {@link ServicePersistenceProvider}, we need at least an account ID. And that must
	 * be valid, otherwise the call is going to fail (CONFLICT).
	 * 
	 * @param accountID	the account ID
	 * @param dataSetID	the dataSet ID
	 * @return			{@link Response} OK or CONFLICT<br />
	 * 					OK with {@link DataSetAggregated} as {@link Entity}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_RESULT_DATASETAGGREGATED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataSetAggregated(@QueryParam(ServiceConfiguration.SVCP_RESULT_ACCOUNTID) String accountID,
										 @QueryParam(ServiceConfiguration.SVCP_RESULT_DATASETID) String dataSetID) {
		
		if (accountID == null || dataSetID == null) {
			return Response.status(Status.CONFLICT).entity("One or more arguments is null.").build();
		}
		
		long accountIDlong = -1L;
		
		try {
			accountIDlong = Long.parseLong(accountID);
		} catch (NumberFormatException nfe) {
			return Response.status(Status.CONFLICT).entity("Account ID not valid long value.").build();
		}
		
		Account account = ServicePersistenceProvider.getInstance().loadAccount(accountIDlong);
		
		if (account == null) {
			return Response.status(Status.CONFLICT).entity("The given account ID is invalid.").build();
		}
		
		try {
			
			DataSetAggregated dsa = AccountPersistenceProvider.createPersistenceProvider(account).loadDataSet(dataSetID);
			return Response.ok(dsa).build();
			
		} catch (DataNotFoundException e) {
			
			LOGGER.info("Cannot find a dataset with id '{}' for accountID '{}'.", dataSetID, accountID);
			return Response.status(Status.CONFLICT).entity("Cannot find a dataset with id " + dataSetID + " for accountID " + accountID + ".").build();
			
		}
	}
	
}
