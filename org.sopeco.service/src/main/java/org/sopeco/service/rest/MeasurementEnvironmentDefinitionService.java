package org.sopeco.service.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ParameterNamespace;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.builder.MeasurementEnvironmentDefinitionBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.shared.ServiceResponse;

/**
 * The <code>MeasurementEnvironmentDefinitionService</code> class provides RESTful services
 * for handling {@link MeasurementEnvironmentDefinition}s (MED) of a {@link ScenarioDefinition}.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_MED)
public class MeasurementEnvironmentDefinitionService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementEnvironmentDefinitionService.class);

	private static final String TOKEN = ServiceConfiguration.SVCP_MED_TOKEN;
	
	/**
	 * Sets the given {@link MeasurementEnvironmentDefinition}s (MED) for the user.
	 * 
	 * @param usertoken the user identification
	 * @param med		the MED
	 * @return			true, if the MED could be set
	 */
	@POST
	@Path(ServiceConfiguration.SVC_MED_SET)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> setMEDefinition(@QueryParam(TOKEN) String usertoken,
								   MeasurementEnvironmentDefinition med) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		return setNewMEDefinition(med, u);
	}
	
	/**
	 * Sets and returns a blank {@link MeasurementEnvironmentDefinition} (MED). <b>Be aware</b>: This method does
	 * also set the MED.
	 * 
	 * 
	 * @param usertoken the user identification
	 * @return			the MED of the given user
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_SET + "/"
		  + ServiceConfiguration.SVC_MED_SET_BLANK)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<MeasurementEnvironmentDefinition> getMEDefinitionFromBlank(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		MeasurementEnvironmentDefinition med = MeasurementEnvironmentDefinitionBuilder.createBlankEnvironmentDefinition();
		setNewMEDefinition(med, u);
		
		ServiceResponse<MeasurementEnvironmentDefinition> m = new ServiceResponse<MeasurementEnvironmentDefinition>(Status.OK, med);
		
		return m;
	}
	
	/**
	 * Returns current selected {@link MeasurementEnvironmentDefinition} (MED) for the given user.
	 * 
	 * @param usertoken the user identification
	 * @return 			current selected MED for the given user
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MED_CURRENT)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<MeasurementEnvironmentDefinition> getCurrentMEDefinition(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<MeasurementEnvironmentDefinition>(Status.UNAUTHORIZED, null);
		}
	
		MeasurementEnvironmentDefinition tmpMED = u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentDefinition();
		
		return new ServiceResponse<MeasurementEnvironmentDefinition>(Status.OK, tmpMED);
	}
	
	/**
	 * Adds a namespace to the currently selected {@link MeasurementEnvironmentDefinition} (MED) of the user.
	 * 
	 * @param usertoken the user identification
	 * @param path		the namespace path
	 * @return			true, if the namespace was added sccessfully
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/" + ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> addNamespace(@QueryParam(TOKEN) String usertoken,
								@QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .addNamespaces(path);

		if (ns == null) {
			return new ServiceResponse<Boolean>(Status.INTERNAL_SERVER_ERROR, false);
		}
		
		if (!storeUserAndScenario(u)) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "cannot store results in database");
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Deletes the namespace with the given path. <b>Be aware</b>:The namespace and all the children are removed here!
	 * 
	 * @param usertoken the user identification
	 * @param path		the namespace path
	 * @return			true, if the namespace was removed successfuly
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/" + ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> removeNamespace(@QueryParam(TOKEN) String usertoken,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .getNamespace(path);

		if (ns == null) {
			LOGGER.warn("Namespace with the path '{}' does not exist!", path);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "namespace does not exist");
		}
		
		u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentBuilder().removeNamespace(ns);
		
		if (!storeUserAndScenario(u)) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "cannot store results in database");
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Renames a namespace at the given path to the new path.
	 * 
	 * @param usertoken the user identification
	 * @param path		the path to the namespace
	 * @param newName	the new path to the namespace
	 * @return			true, if the renaming was successful
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> renameNamespace(@QueryParam(TOKEN) String usertoken,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW) String newName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "namespace does not exist");
		}
		
		u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentBuilder().renameNamespace(ns, newName);
		
		if (!storeUserAndScenario(u)) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "cannot store results in database");
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Adds a parameter to the namespace with the given path.
	 * <b>Attention</b>: The parameter type is stored uppercase!
	 * 
	 * @param usertoken the user identification
	 * @param path		the path to the namespace
	 * @param paramName	the parameter name
	 * @param paramType	the parameter type
	 * @param role		the {@link ParameterRole}
	 * @return			true, if the adding was successful
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> addParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				@QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				@QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				@QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				ParameterRole role) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "namespace does not exist");
		}

		u.getCurrentScenarioDefinitionBuilder()
		 .getMeasurementEnvironmentBuilder()
		 .addParameter(paramName, paramType, role, ns);

		if (!storeUserAndScenario(u)) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "cannot store results in database");
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Updates a parameter in the given path with the new information.
	 * 
	 * @param usertoken 	the user identification
	 * @param path			the path to the parameter
	 * @param paramName		the name of the parameter
	 * @param paramNameNew	the new name of the parameter
	 * @param paramType		the (new) parameter type
	 * @param role			the (new) parameter role
	 * @return				true, if the update was successful
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/" + ServiceConfiguration.SVC_MED_PARAM_UPDATE)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> updateParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME_NEW) String paramNameNew,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				   ParameterRole role) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "namespace does not exist");
		}

		ParameterDefinition parameter = u.getCurrentScenarioDefinitionBuilder()
										 .getMeasurementEnvironmentBuilder()
										 .getParameter(paramName, ns);
		
		if (parameter == null) {
			LOGGER.info("Parameter '{}' does not exist in the namespace with path '{}'!", paramName, path);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "parameter does not exist");
		}
		
		parameter.setName(paramNameNew);
		parameter.setRole(role);
		parameter.setType(paramType);

		if (!storeUserAndScenario(u)) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "cannot store results in database");
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Removes the parameter with the given name in the given path.
	 * 
	 * @param usertoken the user identification
	 * @param path		the path to the parameter
	 * @param paramName	the name of the parameter
	 * @return			true, if the parameter could be removed
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> removeParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return new ServiceResponse<Boolean>(Status.CONFLICT, false, "namespace does not exist");
		}
		
		boolean b = u.getCurrentScenarioDefinitionBuilder()
					 .getMeasurementEnvironmentBuilder()
					 .removeNamespace(ns);

		if (!b) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "namespace removal failed");
		}

		if (!storeUserAndScenario(u)) {
			return new ServiceResponse<Boolean>(Status.ACCEPTED, false, "cannot store results in database");
		}

		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**************************************HELPER****************************************/
	
	/**
	 * Stores the current user state in the service database. The current scenario state for the given
	 * user is stored in the connected account database.
	 * 
	 * @param u 	the user whose information should be stored
	 * @return 		true, if the user and the scenario was stored in the databases
	 */
	private boolean storeUserAndScenario(Users u) {
		// store scenario in account database
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(u.getToken());
		
		if (dbCon == null) {
			LOGGER.warn("Cannot open the account database. Given token is '{}'", u.getToken());
			return false;
		}
		
		dbCon.store(sd);
		dbCon.closeProvider();

		// store user information in Service-database
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return true;
	}
	
	/**
	 * Sets the measurement environment defitinion for the current {@code ScenarioDefinitionBuilder}.
	 * <br />
	 * This method is protected and static as it's called from
	 * {@code MeasurementControllerService.setMEDefinitionFromMEC()}.
	 * 
	 * @param definition 	the MED
	 * @param u 			the user whose MED is to set
	 * @return 				true, if the MED could be stored successfully
	 */
	protected static ServiceResponse<Boolean> setNewMEDefinition(MeasurementEnvironmentDefinition definition, Users u) {
		LOGGER.debug("Set a new measurement environment definition for the user with token '{}'.", u.getToken());
		
		u.getCurrentScenarioDefinitionBuilder().setMeasurementEnvironmentDefinition(definition);
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(u.getToken());
		
		if (dbCon == null) {
			LOGGER.warn("Database connection to account database failed. Cancelling adding MED from MEC to database.");
			return new ServiceResponse<Boolean>(Status.INTERNAL_SERVER_ERROR, false);
		}
		
		dbCon.store(sd);
		dbCon.closeProvider();
		
		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
}
