package org.sopeco.service.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
	
	@POST
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean addScheduledExperiment(@QueryParam(ServiceConfiguration.SVCP_EXECUTE_TOKEN) String usertoken,
										   ScheduledExperiment scheduledExperiment) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		scheduledExperiment.setActive(true);
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
			return false;
		}

		ServicePersistenceProvider.getInstance().storeScheduledExperiment(scheduledExperiment);

		return true;
	}
	
	
	@GET
	@Path(ServiceConfiguration.SVC_EXECUTE_SCHEDULE)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ScheduledExperiment> getScheduledExperiment(@QueryParam(ServiceConfiguration.SVCP_EXECUTE_TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return null;
		}
	
		return ServicePersistenceProvider.getInstance().loadScheduledExperimentsByAccount(u.getCurrentAccount().getId());
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
	public boolean removeScheduledExperiments(@QueryParam(ServiceConfiguration.SVCP_EXECUTE_TOKEN) String usertoken) {
		
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
	
	/**************************************HELPER****************************************/

}
