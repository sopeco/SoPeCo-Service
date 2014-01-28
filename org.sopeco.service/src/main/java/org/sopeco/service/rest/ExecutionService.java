package org.sopeco.service.rest;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.helper.ScheduleExpression;

@Path(ServiceConfiguration.SVC_EXECUTE)
public class ExecutionService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionService.class.getName());
	
	private static final String TOKEN = ServiceConfiguration.SVCP_EXECUTE_TOKEN;
	
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
	
	
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ScheduledExperiment> getScheduledExperiment(@QueryParam(TOKEN) String usertoken) {
		
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
		
		List<ScheduledExperiment> scheduledExperiments = ServicePersistenceProvider.getInstance().loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
		
		for (ScheduledExperiment exp : scheduledExperiments) {
			
			ServicePersistenceProvider.getInstance().removeScheduledExperiment(exp);
			
		}
		
		return true;
	}
	
	
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
	
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_ENABLE)
	@Produces(MediaType.APPLICATION_JSON)
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
	
	
	@PUT
	@Path("{" + ServiceConfiguration.SVCP_EXECUTE_ID + "}" + "/" + ServiceConfiguration.SVC_EXECUTE_DISABLE)
	@Produces(MediaType.APPLICATION_JSON)
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
	
	
	/**************************************HELPER****************************************/

}
