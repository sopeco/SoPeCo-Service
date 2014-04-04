package org.sopeco.service.rest.exchange;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.json.CustomObjectMapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * The {@link ExperimentSeriesRunDecorator} decorates the {@link ExperimentSeriesRun} with overwriting
 * the method {@link #getSuccessfulResultDataSet()}. The overwritten method does call the SoPeCo Service Layer
 * on each method call.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class ExperimentSeriesRunDecorator extends ExperimentSeriesRun {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ExperimentSeriesRunDecorator.class);

	/**
	 * The decorated {@link ExperimentSeriesRun}.
	 */
	//private ExperimentSeriesRun decoratedObject;

	/**
	 * The result of the experiment. As "lazy loading" is used in SoPeCo, this 
	 * object is default <code>null</code>. It's filled once, the result is queried.
	 */
	private DataSetAggregated successfulResultDataSet 	= null;
	
	/**
	 * The prefix to the Service Layer URL.
	 */
	private String serviceHTTPprefix	= "http://";
	
	/**
	 * The host to the Service Layer.
	 */
	private String host					= "localhost";
	
	/**
	 * The port to the Service Layer.
	 */
	private String port					= "8080";
	
	/**
	 * This sign splits the URI information beyond the URL. E.g.
	 * http://host:port/a/b/c, whent the splitting sign is '/',
	 * http://host:port#a#b#c, whent the splitting sign is '#'.
	 */
	private String urlSplitSign			= "/";
	
	/**
	 * This sign splits the host and port in the Service URL.
	 */
	private String hostPortSplitSign	= ":";

	/**
	 * The account ID, to identify at the Service Layer. This is a required parameter
	 * at the Service Layer to fetch the result for this account out of the database
	 * (and not of a random one).
	 */
	private long accountID;

	public ExperimentSeriesRunDecorator() {
	}
	
	/**
	 * Constructor to only pass an {@link ExperimentSeriesRun} which should be decorated.
	 * 
	 * @param decoratedObject 	the {@link ExperimentSeriesRun} to decorate
	 * @param accountID			the account ID to identify at the Service afterwards
	 */
	public ExperimentSeriesRunDecorator(ExperimentSeriesRun decoratedObject, long accountID) {
		super();
		configureSuperclass(decoratedObject);
		//this.decoratedObject = decoratedObject;
		this.accountID		 = accountID;
	}

	/**
	 * Constructor with possibility to change the important Service Layer attributes: Host and port.
	 * The {@link ExperimentSeriesRun} will be decorated.
	 * 
	 * @param decoratedObject	the decorated {@link ExperimentSeriesRun}
	 * @param accountID			the account ID to identify at the Service afterwards
	 * @param host				the host of the Service Layer (default 'localhost')
	 * @param port				the port of the Service Layer (default '8080')
	 */
	public ExperimentSeriesRunDecorator(ExperimentSeriesRun decoratedObject, long accountID, String host, String port) {
		super();
		configureSuperclass(decoratedObject);
		//this.decoratedObject 	= decoratedObject;
		this.accountID		 	= accountID;
		this.host 				= host;
		this.port 				= port;
	}
	
	/**
	 * Constructor with possibility to change all the Service Layer attributes.
	 * The {@link ExperimentSeriesRun} will be decorated.
	 * 
	 * @param decoratedObject	the decorated {@link ExperimentSeriesRun}
	 * @param accountID			the account ID to identify at the Service afterwards
	 * @param host				the host of the Service Layer (default 'localhost')
	 * @param port				the port of the Service Layer (default '8080')
	 * @param serviceHTTPprefix the prefix of the Service Layer (default 'http://')
	 * @param hostPortSplitSign	the split between the host and port in the URL
	 */
	public ExperimentSeriesRunDecorator(ExperimentSeriesRun decoratedObject, long accountID, String host, String port, String serviceHTTPprefix, String urlSplitSign, String hostPortSplitSign) {
		super();
		configureSuperclass(decoratedObject);
		//this.decoratedObject 	= decoratedObject;
		this.accountID		 	= accountID;
		this.host 				= host;
		this.port 				= port;
		this.serviceHTTPprefix 	= serviceHTTPprefix;
		this.urlSplitSign		= urlSplitSign;
		this.hostPortSplitSign 	= hostPortSplitSign;
	}
	
	/**
	 * Configures the super class with the passed {@link ExperimentSeriesRun}.
	 * 
	 * @param decoratedObject the object, which is decorated
	 */
	private void configureSuperclass(ExperimentSeriesRun decoratedObject) {
		super.setSuccessfulResultDataSet(decoratedObject.getSuccessfulResultDataSet());
		super.setTimestamp(decoratedObject.getTimestamp());
		super.setLabel(decoratedObject.getLabel());
		super.setExperimentFailedExceptions(decoratedObject.getExperimentFailedExceptions());
		super.setExperimentSeries(decoratedObject.getExperimentSeries());
	}
	
	/**
	 * The decorators most import method: The overwrite of the {@link #getSuccessfulResultDataSet()}
	 * of the {@link ExperimentSeriesRun}, which is decorated. This method calls the Service Layer
	 * instead of directly accessing the database.
	 */
	@Override
	public DataSetAggregated getSuccessfulResultDataSet() {
		if (successfulResultDataSet == null && getDatasetId() != null) {

			// get dataset from service
			successfulResultDataSet = getDataSetAggregatedFromService(getDatasetId());
			
		}
		
		return successfulResultDataSet;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void storeDataSets(IPersistenceProvider provider) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void removeDataSets(IPersistenceProvider provider) {
	}
	
	/**
	 * Returns null. Does nothing.
	 */
	public IPersistenceProvider getPersistenceProvider() {
		return null;
	}
	
	/**
	 * @return the accountID
	 */
	public long getAccountID() {
		return accountID;
	}
	
	/**
	 * Does nothing.
	 */
	public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
	}
	
	/**
	 * Sets the call to the SoPeCo Service Layer to get the {@link DataSetAggregated}.
	 * 
	 * @param datasetID	the ID to identify the {@link DataSetAggregated}
	 * @return			the {@link DataSetAggregated} to the given ID
	 */
	private DataSetAggregated getDataSetAggregatedFromService(String datasetID) {
		
		logger.info("Setting up call to the Service Layer with datasetID: " + datasetID + ".");
		
		WebTarget webTarget = getClient().target(getFullServiceURL() + urlSplitSign
										+ ServiceConfiguration.SVC_RESULT + urlSplitSign
										+ ServiceConfiguration.SVC_RESULT_DATASETAGGREGATED);
			
		// define parameters to pass to the REST call (can be queryParam and pathParam)
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_RESULT_ACCOUNTID, accountID);
		webTarget = webTarget.queryParam(ServiceConfiguration.SVCP_RESULT_DATASETID, datasetID);
		
		// Null.class, cause Jersey expects an object passed with HTTP POST
		Response r = webTarget.request(MediaType.APPLICATION_JSON).get();
		
		if (r.getStatus() == Status.OK.getStatusCode()) {

			logger.info("Service Layer call successful.");
			return r.readEntity(DataSetAggregated.class);
			
		} else if (r.getStatus() == Status.CONFLICT.getStatusCode()) {

			// when conflict happens, there will be always a error message string in the response
			logger.info("Service Layer call conflicted. Service Layer message: " + r.readEntity(String.class));
			return null;
		}

		logger.info("Service Layer call failed.");
		return null;
	}
	
	/**
	 * Creates a Client with the Jersey Features.
	 * 
	 * @return a Client configured to call the Service Layer
	 */
	private Client getClient() {
		/*ClientConfig config = new ClientConfig();
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());
        config.register(provider);*/
        
		Client client = ClientBuilder.newClient();
		return client;
	}

	/**
	 * Returns the complete URL to the SoPeCo Service Layer from member variables.
	 * 
	 * @return the complete URL to the SPC Service Layer
	 */
	private String getFullServiceURL() {
		return serviceHTTPprefix + host + hostPortSplitSign + port;
	}
	
	@Override
	public String toString() {
		return "ExperimentSeriesRunDecorator{" + "timestamp='" + getTimestamp()  + '\'' + '}';
	}
}
