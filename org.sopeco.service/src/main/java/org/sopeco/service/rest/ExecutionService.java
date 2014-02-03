package org.sopeco.service.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.runner.SoPeCoRunner;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.helper.ScheduleExpression;

/**
 * The <code>ExecutionService</code> class provides the service to {@link ScheduledExperiment}s,
 * manipulate scheduled experiments.<br />
 * The most important feature is the experiment execution of a <code>ScheduledExperiment</code>.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_EXECUTE)
public class ExecutionService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionService.class.getName());
	
	private static final String TOKEN = ServiceConfiguration.SVCP_EXECUTE_TOKEN;
	
	/**
	 * Adds a {@link ScheduledExperiment} to the service.
	 * The last execution time for the schedule is set to <i>-1</i> and the
	 * properties are referenced from the Configuration with the given token.
	 * <br />
	 * <br />
	 * If the schedule is set reapeated, the next valid execution date is calculated. But even
	 * if the ScheduledExperiment is not repeated, the execution time is set.
	 * 
	 * @param usertoken the user identification
	 * @param scheduledExperiment the ScheduledExperiment object
	 * @return true, if the ScheduledExperiement has be added
	 */
	@POST
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean addScheduledExperiment(@QueryParam(TOKEN) String usertoken,
										  ScheduledExperiment scheduledExperiment) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		scheduledExperiment.setLastExecutionTime(-1);
		scheduledExperiment.setProperties(Configuration.getSessionSingleton(usertoken));

		long nextExecution = scheduledExperiment.getStartTime();
		
		if (scheduledExperiment.isRepeating()) {
			nextExecution = ScheduleExpression.nextValidDate(scheduledExperiment.getStartTime(),
															 scheduledExperiment.getRepeatDays(),
															 scheduledExperiment.getRepeatHours(),
															 scheduledExperiment.getRepeatMinutes());
		}
		
		scheduledExperiment.setNextExecutionTime(nextExecution);
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);

		if (dbCon == null) {
			LOGGER.warn("No database connection found.");
			return false;
		}

		ServicePersistenceProvider.getInstance().storeScheduledExperiment(scheduledExperiment);

		return true;
	}
	
	/**
	 * Returns the current list of <code>ScheduledExperiment</code>s for the given account.
	 * <br />
	 * To get a single ScheduledExperiment, see <code>getScheduledExperiment()</code> in
	 * this class.
	 * 
	 * @param usertoken the user identification
	 * @return list of <code>ScheduledExperiment</code>, null if there are none
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ScheduledExperiment> getScheduledExperiments(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return null;
		}
		
		return ServicePersistenceProvider.getInstance().loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
	}
	
	/**
	 * Returns the database ID of the given {@code ScheduledExperiment}. When there is no
	 * matching {@code ScheduledExperiment} found, -1 is returned.
	 * 
	 * This method is PUT rather than GET, because we need to pass a complex object to this
	 * method. This is not possible, when the method is GET.
	 * 
	 * @param usertoken authentification for the user
	 * @param scheduledExperiment the {@code ScheduledExperiment} the ID is searched to
	 * @return >= 0 if a match was found in the database, < 0 if not
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_EXECUTE_ID)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public long getScheduledExperimentID(@QueryParam(TOKEN) String usertoken,
										 ScheduledExperiment scheduledExperiment) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return -1;
		}
	
		List<ScheduledExperiment> list = ServicePersistenceProvider.getInstance()
													.loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
		
		for (ScheduledExperiment se : list) {
			
			if (se.equals(scheduledExperiment)) {
				return se.getId();
			}
			
		}

		LOGGER.info("No scheduled experiement matching to the given one was found in the dabatase.");
		return -1;
	}
	
	/**
	 * Deletes all scheduled experiements for the user with the given token.
	 * 
	 * @param usertoken authentification for the user
	 * @return true, if all scheduled experiements for the user could be deleted
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeScheduledExperiments(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		List<ScheduledExperiment> scheduledExperiments = ServicePersistenceProvider.getInstance()
																				   .loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
		
		for (ScheduledExperiment exp : scheduledExperiments) {
			
			ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);
			
		}
		
		return true;
	}

	/**
	 * Returns the <code>ScheduledExperiment</code> behind the current URI.
	 * 
	 * @param id the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return the <code>ScheduledExperiment</code> behind the current URI
	 */
	@GET
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public ScheduledExperiment getScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
										  		      @QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return null;
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return null;
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return null;
		}
		
		return exp;
	}
	
	/**
	 * Enables the given <code>ScheduledExperiment</code> via it's ID.
	 * 
	 * @param id the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return true, if the ScheduledExperiment could be enabled
	 * 
	 * @deprecated Currently this function is not tested and does not make sense in a RESTful interface
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_ENABLE)
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public boolean setScheduledExperimentEnabled(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
												 @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return false;
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return false;
		}
		
		exp.setActive(true);
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return true;
	}
	
	/**
	 * Disables the given <code>ScheduledExperiment</code> via it's ID.
	 * 
	 * @param id the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return true, if the ScheduledExperiment could be disabled
	 * 
	 * @deprecated Currently this function is not tested and does not make sense in a RESTful interface
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_DISABLE)
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public boolean setScheduledExperimentDisabled(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
												  @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return false;
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return false;
		}
		
		exp.setActive(false);
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return true;
	}
	
	/**
	 * Removes the <code>ScheduledExperiment</code>, which is identified via the ID in the URI.
	 * 
	 * @param id the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return true, if the ScheduledExperiment could be removed
	 */
	@DELETE
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_DELETE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
										     @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return false;
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return false;
		}
		
		ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);
		
		return true;
	}
	
	/**
	 * Executes the <code>ScheduledExperiment</code>, which is identified via the ID in the URI.
	 * This is the entrance method to start the real SoPeCo engine with the parameters given
	 * in the ScheduledExperiment.
	 * 
	 * @param id the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return true, if the experiment was successful executed
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_EXECUTE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean executeScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
										 	  @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return false;
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return false;
		}
		
		// prepare execution
		Map<String, Object> executionProperties = exp.getProperties();
		try {
			
			LOGGER.debug("Experiement settings controller URL: '{}'", exp.getControllerUrl());
			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_URI, new URI(exp.getControllerUrl()));
			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_CLASS_NAME, null); // only if class name is null, URI is searched
			executionProperties.put(IConfiguration.CONF_SCENARIO_DESCRIPTION, exp.getScenarioDefinition());
			
		} catch (URISyntaxException e) {
			LOGGER.error("Invalid controller URL '{}'.", exp.getControllerUrl());
			return false;
		}
		
		// fetch example experiement to execute
		exp.setSelectedExperiments(new ArrayList<String>(Arrays.asList("experimentSeriesDefintion")));
		
		SoPeCoRunner runner  = new SoPeCoRunner(usertoken,
												executionProperties,
												exp.getSelectedExperiments());
		
		Thread t = new Thread(runner);
		
		t.start();
		
		try {
			
			t.join();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the <code>ExecutedExperimentDetails</code> for the user selected scenario.
	 * 
	 * @param usertoken the user identification
	 * @return list of ExecutedExperimentDetails
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_DETAILS)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExecutedExperimentDetails> getExecutedExperimentDetails(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return null;
		}
		
		long accountId = u.getCurrentAccount().getId();
		String scenarioName = u.getAccountDetails().getSelectedScenario();
		
		return ServicePersistenceProvider.getInstance().loadExecutedExperimentDetails(accountId, scenarioName);
	}
	
	/**
	 * Returns the <code>MECLog</code> for the given user and MECLog ID.
	 * 
	 * @param usertoken the user identification
	 * @param id the MECLog ID
	 * 
	 * @return MECLog with the given ID
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_MECLOG)
	@Produces(MediaType.APPLICATION_JSON)
	public MECLog getMECLog(@QueryParam(TOKEN) String usertoken,
						    @QueryParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return null;
		}
		
		return ServicePersistenceProvider.getInstance().loadMECLog(id);
	}
	
}
