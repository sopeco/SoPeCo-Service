package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.runner.SoPeCoRunner;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.execute.ExecutionScheduler;
import org.sopeco.service.execute.QueuedExperiment;
import org.sopeco.service.execute.ScheduleExpression;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.exchange.ExperimentStatus;

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
	 * Adds a {@link ScheduledExperiment} in the service.
	 * The last execution time for the schedule is set to <i>-1</i> and the
	 * properties are referenced from the configuration with the given token.<br />
	 * An integrity check is done for active scenarios: Their selected experiment
	 * list if checked to be valid. If this test fails, the adding fails.
	 * <br />
	 * <br />
	 * If the schedule is set reapeated, the next valid execution date is calculated. But even
	 * if the ScheduledExperiment is not repeated, the execution time is set.
	 * 
	 * @param usertoken 			the user identification
	 * @param scheduledExperiment 	the ScheduledExperiment object
	 * @return 						{@link Response} with status OK, UNAUTHORIZED, CONFLICT or
	 * 								INTERNAL_SERVER_ERROR<br />
	 * 								OK with the experiment key as {@link Long} in the {@link Entity} when
	 * 								the experiment has been set to active state already, OK with the experiment
	 * 								ID as {@link Long}, when the experiment has just been added, but is not in
	 * 								active state.
	 */
	@POST
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addScheduledExperiment(@QueryParam(TOKEN) String usertoken,
						  				   ScheduledExperiment scheduledExperiment) {
		
		if (usertoken == null || scheduledExperiment == null) {
			LOGGER.warn("One or more arguments are null.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		
		// test if the set scheduled experiments is valid for active scenarios
		if (scheduledExperiment.isActive() && !experimentSeriesIsValid(scheduledExperiment)) {
			LOGGER.info("The selected experiments are corrupt.");
			return Response.status(Status.CONFLICT).entity("The selected experiments are corrupt.").build();
		}
		
		// TODO better check for correct MEController URL, rather then only for empty String
		if (scheduledExperiment.getControllerUrl().equals("")) {
			LOGGER.info("The URL to the MeasurementEnvironmentController is invalid.");
			return Response.status(Status.CONFLICT).entity("The URL to the MeasurementEnvironmentController is invalid.").build();
		}
		
		scheduledExperiment.setLastExecutionTime(-1);
		scheduledExperiment.setAddedTime(System.currentTimeMillis());
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
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		ServicePersistenceProvider.getInstance().storeScheduledExperiment(scheduledExperiment);

		// now get the scheduled experiment id, to return it
		List<ScheduledExperiment> list = ServicePersistenceProvider.getInstance()
													.loadScheduledExperimentsByAccount(u.getAccountID());

		for (ScheduledExperiment se : list) {

			if (se.equals(scheduledExperiment)) {
				
				if (se.isActive()) {
					
					LOGGER.info("Experiment successful dispatched in active mode (Experiment key: '{}')", se.getExperimentKey());
					return Response.ok(se.getExperimentKey()).build();
					
				} else {
					
					LOGGER.info("Experiment successful dispatched in inactive mode (Experiment id: '{}')", se.getId());
					return Response.ok(se.getId()).build();
					
				}
				
			}

		}

		LOGGER.warn("ScheduledExperiment was not stored correctly in database. The stored entity cannot be found!");
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Returns the current list of {@link ScheduledExperiment}s for the given account. <br />
	 * To get a single ScheduledExperiment, see {@link #getScheduledExperiment(long, String)} in
	 * this class.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK or UNAUTHORIZED<br />
	 * 					OK with an entity: list of {@link ScheduledExperiment}s (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduledExperiments(@QueryParam(TOKEN) String usertoken) {
		
		if (usertoken == null) {
			LOGGER.warn("One or more arguments are null.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		List<ScheduledExperiment> tmpList =	ServicePersistenceProvider.getInstance().loadScheduledExperimentsByAccount(u.getAccountID());
		
		return Response.ok(tmpList).build();
	}
	
	/**
	 * Deletes all {@link ScheduledExperiment}s for the account related to the user with the given token.
	 * 
	 * @param usertoken authentification for the user
	 * @return 			{@link Response} OK or UNAUTHORIZED
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeScheduledExperiments(@QueryParam(TOKEN) String usertoken) {
		
		if (usertoken == null) {
			LOGGER.warn("One or more arguments are null.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		List<ScheduledExperiment> scheduledExperiments = ServicePersistenceProvider.getInstance()
																				   .loadScheduledExperimentsByAccount(u.getAccountID());
		
		for (ScheduledExperiment exp : scheduledExperiments) {
			
			ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);
			
		}

		return Response.ok().build();
	}

	/**
	 * Returns the <code>ScheduledExperiment</code> behind the current URI.
	 * 
	 * @param id 		the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, CONFLICT or UNAUTHORIZED<br />
	 * 					OK has a {@link ScheduledExperiment} as entity
	 */
	@GET
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
										   @QueryParam(TOKEN) String usertoken) {
		
		if (id < 0 || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return Response.status(Status.CONFLICT).entity("Invalid scheduling id.").build();
		}
		
		if (exp.getAccountId() != u.getAccountID()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		return Response.ok(exp).build();
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
	 * @return 			{@link Response} OK, CONFLICT or UNAUTHORIZED<br />
	 * 					OK has a an Integer (experiment key) as entity
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}"
	      + "/" + ServiceConfiguration.SVC_EXECUTE_ENABLE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setScheduledExperimentEnabled(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
												  @QueryParam(TOKEN) String usertoken) {
		
		if (id < 0 || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return Response.status(Status.CONFLICT).entity("Invalid scheduling id.").build();
		}
		
		if (exp.getAccountId() != u.getAccountID()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		// check if any experimentseriesdefinition have been selected
		List<String> experiments = exp.getSelectedExperiments();
		
		if (experiments == null || experiments.isEmpty()) {
			LOGGER.info("No ExperimentSeriesDefinition selected yet. Cannot start scenario.");
			return Response.status(Status.CONFLICT).entity("No ExperimentSeriesDefinition selected yet."
															+ "Cannot start scenario.").build();
		}	
		
		// this is the crutual line: A background thread analyses the scheduledexperiment in the database for
		// active ones and they are added to the executionqueue
		exp.setActive(true);
		
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return Response.ok(exp.getExperimentKey()).build();
	}
	
	/**
	 * Disables the given <code>ScheduledExperiment</code> via it's ID.
	 * 
	 * @param id 		the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, CONFLICT or UNAUTHORIZED
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_DISABLE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setScheduledExperimentDisabled(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
											  	   @QueryParam(TOKEN) String usertoken) {
		
		if (id < 0 || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return Response.status(Status.CONFLICT).entity("Invalid scheduling id").build();
		}
		
		if (exp.getAccountId() != u.getAccountID()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		exp.setActive(false);
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return Response.ok().build();
	}
	
	/**
	 * Removes the {@link ScheduledExperiment}, which is identified via the ID in the URI.
	 * 
	 * @param id 		the ID of the ScheduledExperiment
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, CONFLICT or UNAUTHORIZED
	 */
	@DELETE
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeScheduledExperiment(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
									     	  @QueryParam(TOKEN) String usertoken) {
		
		if (id < 0 || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return Response.status(Status.CONFLICT).entity("Invalid scheduling id").build();
		}
		
		if (exp.getAccountId() != u.getAccountID()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns the current status of the experiment with the given key. Both, the table for {@link ScheduledExperiment}s
	 * and the one for {@link QueuedExperiment}s is searched for the given key. This implies, that this
	 * method can be called for active and inactive {@link ScheduledExperiment}s.
	 * 
	 * @param experimentKey	the experiment key
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK, CONFLICT or UNAUTHORIZED<br />
	 * 						OK with the {@link ExperimentStatus} as entity
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_STATUS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduledExperimentStatus(@QueryParam(ServiceConfiguration.SVCP_EXECUTE_KEY) int experimentKey,
									     		 @QueryParam(TOKEN) String usertoken) {
		
		if (usertoken == null) {
			LOGGER.warn("Given usertoken is null.");
			return Response.status(Status.CONFLICT).entity("Given usertoken is null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ExperimentStatus status = ExecutionScheduler.getInstance().getExperimentStatus(experimentKey);
		
		// if the status is null, the key must be corrupt, because the table ScheduledExepriment
		// and the QueuedExperiment is searched for the key.
		if (status == null) {
			LOGGER.info("The experiment key is corrupt.", usertoken);
			return Response.status(Status.CONFLICT).entity("The experiment key is corrupt.").build();
		}

		return Response.ok(status).build();
	}
	
	/**
	 * Sets the status of the experiment with the given key to abort. The feature may take up a while,
	 * as it's set via a configuration property, which is read in the {@link SoPeCoRunner}.
	 * Be aware, that the experiment might not be in execution yet, but the status can change anyway.
	 * 
	 * @param experimentKey	the experiment key
	 * @param usertoken		the user identification
	 * @return				{@link Response} OK or UNAUTHORIZED
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_EXECUTE_ABORT)
	@Produces(MediaType.APPLICATION_JSON)
	public Response abortScheduledExperiment(@QueryParam(ServiceConfiguration.SVCP_EXECUTE_KEY) long experimentKey,
									     	 @QueryParam(TOKEN) String usertoken) {
		
		if (usertoken == null) {
			LOGGER.warn("Given usertoken is null.");
			return Response.status(Status.CONFLICT).entity("Given usertoken is null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ExecutionScheduler.getInstance().setExperimentAborting(experimentKey);

		return Response.ok().build();
	}
	
	/**
	 * Removes the {@link ExperimentSeriesDefinition} with the given name from the list
	 * of selected experiments in the {@link ScheduledExperiment} with the given ID.
	 * 
	 * @param id					the ID of the {@link ScheduledExperiment}
	 * @param experimentseriesname	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @return						{@link Response} OK, CONFLICT or UNAUTHORIZED
	 */
	@DELETE
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_ESD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeSelectedExperimentSeriesDefinition(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
								  	  				 		 @QueryParam(ServiceConfiguration.SVCP_EXECUTE_EXPERIMENTSERIES) String experimentseriesname,
							  	  				 		 	 @QueryParam(TOKEN) String usertoken) {
		
		if (id < 0 || experimentseriesname == null || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return Response.status(Status.CONFLICT).entity("invalid scheduling id").build();
		}
		
		if (exp.getAccountId() != u.getAccountID()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return Response.status(Status.UNAUTHORIZED).build();
		}

		if (exp.isActive()) {
			LOGGER.info("The experiment is active and cannot be modified.");
			return Response.status(Status.CONFLICT).entity("Experiment is active and cannot be modified.").build();
		}
		
		if (exp.getSelectedExperiments().size() == 1 && 
				exp.getSelectedExperiments().get(0).equals(experimentseriesname)) {
			LOGGER.info("Cannot delete the only ESD in this experiment.");
			return Response.status(Status.CONFLICT).entity("Cannot delete the only ESD in this experiment.").build();
		}
		
		// check for the name in the active ESD list
		for (String esdName : exp.getSelectedExperiments()) {
			
			if (esdName.equals(experimentseriesname)) {
				exp.getSelectedExperiments().remove(esdName);
				break;
			}
			
		}
		
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);

		return Response.ok().build();
	}
	
	/**
	 * Sets the {@link ExperimentSeriesDefinition} with the given name to the experiment which should be executed.
	 * 
	 * @param id					the experiment ID
	 * @param experimentseriesname	the name of the {@link ExperimentSeriesDefinition}
	 * @param usertoken				the user identification
	 * @return						{@link Response} OK, CONFLICT or UNAUTHORIZED<br />
	 */
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}"
		  + "/" + ServiceConfiguration.SVC_EXECUTE_ESD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response selectExperimentSeriesDefinition(@PathParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id,
								  	  				 @QueryParam(ServiceConfiguration.SVCP_EXECUTE_EXPERIMENTSERIES) String experimentseriesname,
						  	  				 		 @QueryParam(TOKEN) String usertoken) {

		if (id < 0 || experimentseriesname == null || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ScheduledExperiment exp = ServicePersistenceProvider.getInstance().loadScheduledExperiment(id);
		
		if (exp == null) {
			LOGGER.info("Invalid scheduling id '{}'.", id);
			return Response.status(Status.CONFLICT).entity("invalid scheduling id").build();
		}

		if (exp.getAccountId() != u.getAccountID()) {
			LOGGER.info("The scheduled experiment is not from the account, this user relates to. Perimission denied.");
			return Response.status(Status.UNAUTHORIZED).build();
		}

		if (exp.isActive()) {
			LOGGER.info("The experiment is active and cannot be modified.");
			return Response.status(Status.CONFLICT).entity("Experiment is active and cannot be modified.").build();
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
			return Response.status(Status.CONFLICT).entity("There is no ExperimentSeriesDefinition"
															+ " with the given name.").build();
		}

		List<String> experimentList = exp.getSelectedExperiments();
		
		if (experimentList == null) {
			experimentList = new ArrayList<String>();
		}
		
		experimentList.add(experimentseriesname);
		
		ServicePersistenceProvider.getInstance().storeScheduledExperiment(exp);
		
		return Response.ok().build();
	}
	
	/**
	 * Returns the <code>ExecutedExperimentDetails</code> for the user selected scenario.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK, UNAUTHORIZED or CONFLICT<br />
	 * 					OK with list of {@link ExecutedExperimentDetails} (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_DETAILS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExecutedExperimentDetails(@QueryParam(TOKEN) String usertoken,
												 @QueryParam(ServiceConfiguration.SVCP_EXECUTE_SCENARIONAME) String scenarioname) {
		
		if (scenarioname == null || usertoken == null) {
			LOGGER.warn("One or more arguments are null.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		long accountId = u.getAccountID();
		
		List<ExecutedExperimentDetails> tmpList = ServicePersistenceProvider.getInstance().loadExecutedExperimentDetails(accountId, scenarioname);
	
		return Response.ok(tmpList).build();
	}
	
	/**
	 * Returns the <code>MECLog</code> for the given user and MECLog ID.
	 * 
	 * @param usertoken the user identification
	 * @param id 		the MECLog ID
	 * @return 			{@link Response} OK or UNAUTHORIZED<br />
	 * 					OK with {@link MECLog} entity (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_MECLOG)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMECLog(@QueryParam(TOKEN) String usertoken,
				    		  @QueryParam(ServiceConfiguration.SVCP_EXECUTE_ID) long id) {
		
		if (id < 0 || usertoken == null) {
			LOGGER.warn("One or more arguments are null/invalid.");
			return Response.status(Status.CONFLICT).entity("One or more arguments are null/invalid.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		MECLog tmpMECLog = ServicePersistenceProvider.getInstance().loadMECLog(id);

		return Response.ok(tmpMECLog).build();
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
		
		LOGGER.info("Checking ESD of Scenario ' {}' for validity.", experiment.getScenarioDefinition().getScenarioName());
		
		// first check for a valid selected experiment list
		List<String> selectedExperiment = experiment.getSelectedExperiments();
		
		if (selectedExperiment == null || selectedExperiment.isEmpty()) {
			LOGGER.debug("ESD is null or empty.");
			return false;
		}
		
		// second check for valid selected names
		boolean validName = false;

		for (String experimentName : selectedExperiment) {

			// WHY THE HACK SPLITTED WITH '.'???? TODO! Check SPC Core
			String[] experimentSplitted = experimentName.split(Pattern.quote( "." ));
		
			if (experimentSplitted.length <= 1) {
				LOGGER.debug("ESD name corrupt: '{}'.", experimentName);
				return false;
			}
			
			String msName 	= experimentSplitted[0].trim();
			String esdName 	= experimentSplitted[1].trim();
			
			LOGGER.debug("Checking selected ESD '{}', if it is in the list of all ESD of the ScenarioDefinition.", esdName);
			LOGGER.debug("The ESD is in the MeasurementSpecification with name '{}'.", msName);
			
			for (ExperimentSeriesDefinition esd : experiment.getScenarioDefinition().getAllExperimentSeriesDefinitions()) {
				
				if (esd.getName().equals(esdName)) {
					LOGGER.debug("ESD '{}' is a valid ESD name.", esdName);
					validName = true;
				}
				
			}
			
			// only if no experimentSeries with the given name was found
			if (!validName) {
				LOGGER.debug("The ESD '{}' not found in list of all ESD in MeasurementSpecifications.", esdName);
				return false;
			}
			
			validName = false;
			
		}
		
		return true;
	}
	
}
