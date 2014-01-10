package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.Users;

@Path(ServiceConfiguration.SVC_MEASUREMENT)
public class MeasurementSpecificationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementSpecificationService.class);
	
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LIST)
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getAllSpecificationNames(@QueryParam("token") String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		List<String> returnList = new ArrayList<String>();
		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder().getBuiltScenario().getMeasurementSpecifications()) {
			returnList.add(ms.getName());
		}

		return returnList;
	}
	
	@GET
	@Path(ServiceConfiguration.SVC_MEASUREMENT_LISTSPECS)
	@Produces(MediaType.APPLICATION_JSON)
	public List<MeasurementSpecification> getAllSpecifications(@QueryParam("token") String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		List<MeasurementSpecification> returnList = new ArrayList<MeasurementSpecification>();
		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder().getBuiltScenario()
				.getMeasurementSpecifications()) {
			returnList.add(ms);
		}

		return returnList;
	}
	
	@PUT
	@Path(ServiceConfiguration.SVC_MEASUREMENT_SWITCH)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean setWorkingSpecification(@QueryParam("token") String usertoken,
										   @QueryParam("specname") String specificationName) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		LOGGER.debug("Set working specification on: " + specificationName);

		if (!existSpecification(specificationName, u)) {
			LOGGER.debug("Can't set working specification to '{}' because it doesn't exists. ", specificationName);
			return false;
		}
		
		u.setWorkingSpecification(specificationName);
		
		return true;
	}
	
	
	
	
	
	
	
	
	// *************************** (private) HELPER *****************************************
	
	/**
	 * Returns whether a specification with the given name exists.
	 * 
	 * @param specification specififcation name
	 * @return true, if specification exists
	 */
	private boolean existSpecification(String specification, Users u) {

		for (MeasurementSpecification ms : u.getCurrentScenarioDefinitionBuilder().getBuiltScenario()
				.getMeasurementSpecifications()) {
			if (specification.equals(ms.getName())) {
				return true;
			}
		}
		
		return false;
	}
}
