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
import org.sopeco.service.persistence.UserPersistenceProvider;
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
			
			DataSetAggregated dsa = UserPersistenceProvider.createPersistenceProvider(account).loadDataSet(dataSetID);
			return Response.ok(dsa).build();
			
		} catch (DataNotFoundException e) {
			
			LOGGER.info("Cannot find a dataset with id '{}' for accountID '{}'.", dataSetID, accountID);
			return Response.status(Status.CONFLICT).entity("Cannot find a dataset with id " + dataSetID + " for accountID " + accountID + ".").build();
			
		}
	}
	
}
