package org.sopeco.service.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ParameterNamespace;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.builder.MeasurementEnvironmentDefinitionBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.entities.Users;

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
	 * @return			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@POST
	@Path(ServiceConfiguration.SVC_MED_SET)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setMEDefinition(@QueryParam(TOKEN) String usertoken,
								    MeasurementEnvironmentDefinition med) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		boolean b = ServiceStorageModul.setNewMeasurementEnvironmentDefinition(med, u);
		
		if (!b) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store MED in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Sets and returns a blank {@link MeasurementEnvironmentDefinition} (MED). <b>Be aware</b>: This method does
	 * also set the MED.
	 * 
	 * @param usertoken the user identification
	 * @return			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR<br />
	 * 					OK with a blank {@link MeasurementEnvironmentDefinition} as {@link Entity}
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_SET + "/"
		  + ServiceConfiguration.SVC_MED_SET_BLANK)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMEDefinitionFromBlank(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		MeasurementEnvironmentDefinition med = MeasurementEnvironmentDefinitionBuilder.createBlankEnvironmentDefinition();
		
		boolean b = ServiceStorageModul.setNewMeasurementEnvironmentDefinition(med, u);
		
		if (!b) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store MED in database.").build();
		}

		return Response.ok(med).build();
	}
	
	/**
	 * Returns current selected {@link MeasurementEnvironmentDefinition} (MED) for the given user.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK or UNAUTHORIZED<br />
	 * 					OK with {@link MeasurementEnvironmentDefinition} as {@link Entity}
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MED_CURRENT)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrentMEDefinition(@QueryParam(TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
	
		MeasurementEnvironmentDefinition tmpMED = u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentDefinition();

		return Response.ok(tmpMED).build();
	}
	
	/**
	 * Adds a namespace to the currently selected {@link MeasurementEnvironmentDefinition} (MED) of the user.
	 * 
	 * @param usertoken the user identification
	 * @param path		the namespace path
	 * @return			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/" + ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNamespace(@QueryParam(TOKEN) String usertoken,
								 @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .addNamespaces(path);

		if (ns == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (!ServiceStorageModul.storeUserAndScenario(u)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Deletes the namespace with the given path. <b>Be aware</b>:The namespace and all the children are removed here!
	 * 
	 * @param usertoken the user identification
	 * @param path		the namespace path
	 * @return			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/" + ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeNamespace(@QueryParam(TOKEN) String usertoken,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .getNamespace(path);

		if (ns == null) {
			LOGGER.warn("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}
		
		u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentBuilder().removeNamespace(ns);
		
		if (!ServiceStorageModul.storeUserAndScenario(u)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Renames a namespace at the given path to the new path.
	 * 
	 * @param usertoken the user identification
	 * @param path		the path to the namespace
	 * @param newName	the new path to the namespace
	 * @return			{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameNamespace(@QueryParam(TOKEN) String usertoken,
								    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
								    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW) String newName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}
		
		u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentBuilder().renameNamespace(ns, newName);
		
		if (!ServiceStorageModul.storeUserAndScenario(u)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
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
	 * @return			{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/" + ServiceConfiguration.SVC_MED_PARAM_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				 @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				 @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				 @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				 ParameterRole role) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}

		u.getCurrentScenarioDefinitionBuilder()
		 .getMeasurementEnvironmentBuilder()
		 .addParameter(paramName, paramType, role, ns);

		if (!ServiceStorageModul.storeUserAndScenario(u)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
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
	 * @return				{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/" + ServiceConfiguration.SVC_MED_PARAM_UPDATE)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME_NEW) String paramNameNew,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				    ParameterRole role) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}

		ParameterDefinition parameter = u.getCurrentScenarioDefinitionBuilder()
										 .getMeasurementEnvironmentBuilder()
										 .getParameter(paramName, ns);
		
		if (parameter == null) {
			LOGGER.info("Parameter '{}' does not exist in the namespace with path '{}'!", paramName, path);
			return Response.status(Status.CONFLICT).entity("Parameter does not exist.").build();
		}
		
		/*ConstantValueAssignment initialAssignmentParameter = null;
		for (ConstantValueAssignment cva : u.getCurrentScenarioDefinitionBuilder()
											.getMeasurementSpecificationBuilder()
											.getBuiltSpecification()
											.getInitializationAssignemts()) {
			
			if (cva.getParameter().getFullName().equals(parameter.getFullName())) {
				initialAssignmentParameter = cva;
			}
			
		}

		if (initialAssignmentParameter != null) {
			initialAssignmentParameter.setParameter(parameter);
			MainLayoutPanel.get().getController(SpecificationController.class).addExistingAssignments();
		}*/
		
		parameter.setName(paramNameNew);
		parameter.setRole(role);
		parameter.setType(paramType);

		if (!ServiceStorageModul.storeUserAndScenario(u)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Removes the parameter with the given name in the given path.
	 * 
	 * @param usertoken the user identification
	 * @param path		the path to the parameter
	 * @param paramName	the name of the parameter
	 * @return			{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}
		
		boolean b = u.getCurrentScenarioDefinitionBuilder()
					 .getMeasurementEnvironmentBuilder()
					 .removeNamespace(ns);

		if (!b) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot remove namespace.").build();
		}

		if (!ServiceStorageModul.storeUserAndScenario(u)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
}
