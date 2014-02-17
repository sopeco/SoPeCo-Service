package org.sopeco.service.rest;

import java.util.ArrayList;
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
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.execute.ExecutionScheduler;
import org.sopeco.service.execute.ScheduleExpression;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.exchange.ServiceResponse;

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
	 * properties are referenced from the Configuration with the given token.<br />
	 * An integrity check is done for active scenarios: Their selected experiment
	 * list if checked to be valid. If this test fails, the adding fails.
	 * <br />
	 * <br />
	 * If the schedule is set reapeated, the next valid execution date is calculated. But even
	 * if the ScheduledExperiment is not repeated, the execution time is set.
	 * 
	 * @param usertoken 			the user identification
	 * @param scheduledExperiment 	the ScheduledExperiment object
	 * @return 						true, if the ScheduledExperiement has be added
	 */
	@POST
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> addScheduledExperiment(@QueryParam(TOKEN) String usertoken,
										  				   ScheduledExperiment scheduledExperiment) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		// test if the set scheduled experiments is valid for active scenarios
		if (scheduledExperiment.isActive() && !experimentSeriesIsValid(scheduledExperiment)) {
			LOGGER.info("The selected experiments are corrupt.");
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "The selected experiments are corrupt.");
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
			return new ServiceResponse<Boolean>(Status.INTERNAL_SERVER_ERROR, false);
		}

		ServicePersistenceProvider.getInstance().storeScheduledExperiment(scheduledExperiment);

		return new ServiceResponse<Boolean>(Status.OK, true);
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
	public ServiceResponse<List<ScheduledExperiment>> getScheduledExperiments(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return null;
		}
		
		List<ScheduledExperiment> tmpList =	ServicePersistenceProvider.getInstance().loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
		
		return new ServiceResponse<List<ScheduledExperiment>>(Status.OK, tmpList);
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
	public ServiceResponse<Long> getScheduledExperimentID(@QueryParam(TOKEN) String usertoken,
										 				  ScheduledExperiment scheduledExperiment) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Long>(Status.UNAUTHORIZED, new Long(-1));
		}
	
		List<ScheduledExperiment> list = ServicePersistenceProvider.getInstance()
													.loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
		
		for (ScheduledExperiment se : list) {
			
			if (se.equals(scheduledExperiment)) {
				return new ServiceResponse<Long>(Status.OK, new Long(se.getId()));
			}
			
		}

		LOGGER.info("No scheduled experiement matching to the given one was found in the dabatase.");
		return new ServiceResponse<Long>(Status.NO_CONTENT, new Long(-1));
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
	public ServiceResponse<Boolean> removeScheduledExperiments(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		List<ScheduledExperiment> scheduledExperiments = ServicePersistenceProvider.getInstance()
																				   .loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
		
		for (ScheduledExperiment exp : scheduledExperiments) {
			
			ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);
			
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
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
	public ServiceResponse<ScheduledExperiment> getScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
										  		      				   @QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<ScheduledExperiment>(Status.UNAUTHORIZED, null);
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return new ServiceResponse<ScheduledExperiment>(Status.CONFLICT, null, "Invalid scheduling id.");
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return new ServiceResponse<ScheduledExperiment>(Status.UNAUTHORIZED, null, "Others users experiment!");
		}
		
		return new ServiceResponse<ScheduledExperiment>(Status.OK, exp);
	}
	
	
	
	/**
	 * Enables the given {@link ScheduledExperiment} via it's ID. The experiment is only
	 * activated, if it has {@link ExperimentSeriesDefinition} selected.<br />
	 * <br />
	 * A background progress is running ({@link ExecutionScheduler}) and checks for
	 * active experiments and adds them to the execution queue. Of course, the are only added
	 * if the start time for the experiment is reached.
	 * 
	 * @param id 		the ID of the {@link ScheduledExperiment}
	 * @param usertoken the user identification
	 * @return 			true, if the {@link ScheduledExperiment} could be enabled
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}"
	      + "/" + ServiceConfiguration.SVC_EXECUTE_ENABLE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Integer> setScheduledExperimentEnabled(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
												 				  @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Integer>(Status.UNAUTHORIZED, -1);
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return new ServiceResponse<Integer>(Status.CONFLICT, -1);
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return new ServiceResponse<Integer>(Status.UNAUTHORIZED, -1);
		}
		
		// check if any experimentseriesdefinition have been selected
		List<String> experiments = exp.getSelectedExperiments();
		
		if (experiments == null || experiments.isEmpty()) {
			LOGGER.info("No ExperimentSeriesDefinition selected yet. Cannot start scenario.");
			return new ServiceResponse<Integer>(Status.CONFLICT, -1);
		}	
		
		// this is the crutual line. A thread analyses the scheduledexperiment for active one and they are added
		// to the execution list
		exp.setActive(true);
		
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return new ServiceResponse<Integer>(Status.OK, exp.getExperimentKey());
	}
	
	/**
	 * Disables the given <code>ScheduledExperiment</code> via it's ID.
	 * 
	 * @param id the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return true, if the ScheduledExperiment could be disabled
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_DISABLE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> setScheduledExperimentDisabled(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
											  					   @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return new ServiceResponse<Boolean>(Status.NO_CONTENT, false);
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		exp.setActive(false);
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return new ServiceResponse<Boolean>(Status.OK, true);
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
	public ServiceResponse<Boolean> removeScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
									     					  @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return new ServiceResponse<Boolean>(Status.NO_CONTENT, false);
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Removes the {@link ExperimentSeriesDefinition} with the given name from the list
	 * of selected experiments in the {@link ScheduledExperiment} with the given ID.
	 * 
	 * @param id					the ID of the {@link ScheduledExperiment}
	 * @param experimentseriesname	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @return						true, if the {@link ExperimentSeriesDefinition} is removed
	 */
	@DELETE
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}"
		  + "/" + ServiceConfiguration.SVC_EXECUTE_ESD)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> removeSelectedExperimentSeriesDefinition(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
												  	  				 		 @QueryParam(ServiceConfiguration.SVCP_EXECUTE_EXPERIMENTSERIES) String experimentseriesname,
											  	  				 		 	 @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "invalid scheduling id");
		}
		
		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		// check for the name in the active ESD list	
		for (String esdName : exp.getSelectedExperiments()) {
			
			if (esdName.equals(experimentseriesname)) {
				exp.getSelectedExperiments().remove(esdName);
				break;
			}
			
		}
		
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Sets the {@link ExperimentSeriesDefinition} with the given name to the experiment which should be executed.
	 * 
	 * @param id					the experiment ID
	 * @param experimentseriesname	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @return						true, if the {@link ExperimentSeriesDefinition} was added
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}"
		  + "/" + ServiceConfiguration.SVC_EXECUTE_ESD)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> selectExperimentSeriesDefinition(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
												  	  				 @QueryParam(ServiceConfiguration.SVCP_EXECUTE_EXPERIMENTSERIES) String experimentseriesname,
										  	  				 		 @QueryParam(TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "invalid scheduling id");
		}

		if (exp.getAccountId() != u.getCurrentAccount().getId()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		// check if experiment series name is valid
		boolean validName = false;

		for (ExperimentSeriesDefinition esd : exp.getScenarioDefinition().getAllExperimentSeriesDefinitions()) {
			
			if (esd.getName().equals(experimentseriesname)) {
				validName = true;
				break;
			}
			
		}

		if (!validName) {
			LOGGER.info("There is no ExperimentSeriesDefinition with the given name.");
			return new ServiceResponse<Boolean>(Status.CONFLICT, false);
		}

		List<String> experimentList = exp.getSelectedExperiments();
		
		if (experimentList == null) {
			experimentList = new ArrayList<String>();
		}
		
		experimentList.add(experimentseriesname);
		
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);

		return new ServiceResponse<Boolean>(Status.OK, true);
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
	public ServiceResponse<List<ExecutedExperimentDetails>> getExecutedExperimentDetails(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<List<ExecutedExperimentDetails>>(Status.UNAUTHORIZED);
		}
		
		long accountId = u.getCurrentAccount().getId();
		String scenarioName = u.getAccountDetails().getSelectedScenario();
		
		List<ExecutedExperimentDetails> tmpList = ServicePersistenceProvider.getInstance().loadExecutedExperimentDetails(accountId, scenarioName);
		
		return new ServiceResponse<List<ExecutedExperimentDetails>>(Status.OK, tmpList);
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
	public ServiceResponse<MECLog> getMECLog(@QueryParam(TOKEN) String usertoken,
				    			    		 @QueryParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<MECLog>(Status.UNAUTHORIZED);
		}
		
		MECLog tmpMECLog = ServicePersistenceProvider.getInstance().loadMECLog(id);
		
		return new ServiceResponse<MECLog>(Status.OK, tmpMECLog);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// HELPER ///////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns whether the given {@link ScheduledExperiment} has a valid list of
	 * scheduled experiments. The integrity test first tests if the list as such
	 * is valid. If the list is valid, every entry in the list is checked to
	 * maps to an existing {@link ExperimentSeriesDefinition} in the underlying
	 * {@link ScenarioDefinition}.
	 * 
	 * @param experiment	the {@link ScheduledExperiment} to check
	 * @return				true, if the scheduled experiment list is valid
	 */
	private boolean experimentSeriesIsValid(ScheduledExperiment experiment) {
		
		// first check for a valid selected experiment list
		List<String> selectedExperiment = experiment.getSelectedExperiments();
		
		if (selectedExperiment == null || selectedExperiment.isEmpty()) {
			return false;
		}
		
		// second check for valid selected names
		boolean validName = false;

		for (String experimentName : selectedExperiment) {
			
			for (ExperimentSeriesDefinition esd : experiment.getScenarioDefinition().getAllExperimentSeriesDefinitions()) {
				
				if (esd.getName().equals(experimentName)) {
					validName = true;
				}
				
			}
			
			// only if no experimentSeries with the given name was found
			if (!validName) {
				return false;
			}
			
			validName = false;
			
		}
		
		return true;
	}
	
	
}
