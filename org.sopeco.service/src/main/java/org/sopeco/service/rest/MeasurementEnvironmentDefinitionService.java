package org.sopeco.service.rest;

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
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ParameterNamespace;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.SimpleEntityFactory;
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

	/**
	 * A shortener, because the token needs to be provided on every single RESTful interface.
	 */
	private static final String TOKEN = ServiceConfiguration.SVCP_MED_TOKEN;
	
	/**
	 * A shortener for the scenario name, as it's must be provided on every REST interface call.
	 */
	private static final String SCENARIONAME = ServiceConfiguration.SVC_MED_SCENARIONAME;
	
	/**
	 * Sets the given {@link MeasurementEnvironmentDefinition}s (MED) to the given {@link ScenarioDefinition}.<br />
	 * Be sure, that the passed {@link MeasurementEnvironmentDefinition} has a root namespace set, otherwise
	 * a CONFLICT is thrown.
	 * 
	 * @param usertoken 	the user identification
	 * @param scenarioName	the name of the scenario
	 * @param med			the MED
	 * @return				{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@POST
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_SET)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setMeasurementEnvironmentDefinition(@QueryParam(TOKEN) String usertoken,
														@PathParam(SCENARIONAME) String scenarioName,
								    					MeasurementEnvironmentDefinition med) {
		
		if (med == null || usertoken == null || scenarioName == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		if (med.getRoot() == null) {
			LOGGER.warn("MeasurementEnvironmentDefinition has no root namespace set.", usertoken);
			return Response.status(Status.CONFLICT).entity("MED has no root namespace set.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		sd.setMeasurementEnvironmentDefinition(med);
		
		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}
		
		return Response.ok().build();
	}
	
	/**
	 * Sets and returns a blank {@link MeasurementEnvironmentDefinition} (MED). <b>Be aware</b>: This method does
	 * also set the MED.
	 * 
	 * @param usertoken 	the user identification
	 * @param scenarioName	the name of the scenario
	 * @return				{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR<br />
	 * 						OK with a blank {@link MeasurementEnvironmentDefinition} as {@link Entity}
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_SET + "/"
			+ ServiceConfiguration.SVC_MED_SET_BLANK)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMEDefinitionFromBlank(@QueryParam(TOKEN) String usertoken,
											 @PathParam(SCENARIONAME) String scenarioName) {
		
		if (usertoken == null || scenarioName == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		MeasurementEnvironmentDefinition med = SimpleEntityFactory.createDefaultMeasurementEnvironmentDefinition();
		
		sd.setMeasurementEnvironmentDefinition(med);
		
		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}
		
		return Response.ok().build();
		
	}
	
	/**
	 * Returns current selected {@link MeasurementEnvironmentDefinition} (MED) for the given scenario (via scenario name).
	 * 
	 * @param usertoken 	the user identification
	 * @param scenarioName	the name of the scenario
	 * @return 				{@link Response} OK or UNAUTHORIZED<br />
	 * 						OK with {@link MeasurementEnvironmentDefinition} as {@link Entity}
	 */
	@GET
	@Path("{" + SCENARIONAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMEDefinition(@QueryParam(TOKEN) String usertoken,
									@PathParam(SCENARIONAME) String scenarioName) {
		
		if (usertoken == null || scenarioName == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
	
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		return Response.ok(sd.getMeasurementEnvironmentDefinition()).build();
	}
	
	/**
	 * Adds a namespace to the {@link MeasurementEnvironmentDefinition} (MED) connected the the {@link ScenarioDefinition}
	 * given via the name.
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken 	the user identification
	 * @param path			the namespace path
	 * @return				{@link Response} OK, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNamespace(@PathParam(SCENARIONAME) String scenarioName,
								 @QueryParam(TOKEN) String usertoken,
								 @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		if (scenarioName == null || usertoken == null || path == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		ParameterNamespace ns = addNamespaces(sd.getMeasurementEnvironmentDefinition().getRoot(), path);

		if (ns == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Deletes the namespace with the given path. <b>Be aware</b>:The namespace and all the children are removed here!
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken 	the user identification
	 * @param path			the namespace path
	 * @return				{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeNamespace(@PathParam(SCENARIONAME) String scenarioName,
			 					 	@QueryParam(TOKEN) String usertoken,
								    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		if (scenarioName == null || usertoken == null || path == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		ParameterNamespace ns = getNamespace(sd.getMeasurementEnvironmentDefinition().getRoot(), path);

		if (ns == null) {
			LOGGER.warn("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}
		
		removeNamespace(ns);
		
		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Renames a {@link ParameterNamespace} at the given path to the new path.
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken 	the user identification
	 * @param path			the path to the namespace
	 * @param newName		the new path to the namespace
	 * @return				{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public Response renameNamespace(@PathParam(SCENARIONAME) String scenarioName,
									@QueryParam(TOKEN) String usertoken,
								    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
								    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW) String newName) {
		
		if (scenarioName == null || usertoken == null || path == null || newName == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}

		ParameterNamespace ns = getNamespace(sd.getMeasurementEnvironmentDefinition().getRoot(), path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}
		
		renameNamespace(ns, newName);
		
		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Adds a parameter to the {@link ParameterNamespace} with the given path.
	 * <b>Attention</b>: The parameter type is stored uppercase!
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken the user identification
	 * @param path		the path to the namespace
	 * @param paramName	the parameter name
	 * @param paramType	the parameter type
	 * @param role		the {@link ParameterRole}
	 * @return			{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addParameter(@PathParam(SCENARIONAME) String scenarioName,
							     @QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				 @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				 @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				 @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				 ParameterRole role) {
		
		if (scenarioName == null || usertoken == null || path == null
				|| paramName == null || paramType == null || role == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = getNamespace(sd.getMeasurementEnvironmentDefinition().getRoot(), path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}

		addParameter(paramName, paramType, role, ns);

		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Updates a parameter in the given path with the new information.
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken 	the user identification
	 * @param path			the path to the parameter
	 * @param paramName		the name of the parameter
	 * @param paramNameNew	the new name of the parameter
	 * @param paramType		the (new) parameter type
	 * @param role			the (new) parameter role
	 * @return				{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@PUT
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_UPDATE)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateParameter(@PathParam(SCENARIONAME) String scenarioName,
									@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME_NEW) String paramNameNew,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				    ParameterRole role) {
		
		if (scenarioName == null || usertoken == null || path == null || paramNameNew == null
				|| paramName == null || paramType == null || role == null) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = getNamespace(sd.getMeasurementEnvironmentDefinition().getRoot(), path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}

		ParameterDefinition parameter = getParameter(paramName, ns);
		
		if (parameter == null) {
			LOGGER.info("Parameter '{}' does not exist in the namespace with path '{}'!", paramName, path);
			return Response.status(Status.CONFLICT).entity("Parameter does not exist.").build();
		}
		
		parameter.setName(paramNameNew);
		parameter.setRole(role);
		parameter.setType(paramType);

		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	
	/**
	 * Removes the parameter with the given name in the given path.
	 * 
	 * @param scenarioName	the name of the scenario
	 * @param usertoken the user identification
	 * @param path		the path to the parameter
	 * @param paramName	the name of the parameter
	 * @return			{@link Response} OK, CONFLICT, UNAUTHORIZED or INTERNAL_SERVER_ERROR
	 */
	@DELETE
	@Path("{" + SCENARIONAME + "}" + "/"
			+ ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeParameter(@PathParam(SCENARIONAME) String scenarioName,
									@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				    @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName) {
		
		if (scenarioName == null || usertoken == null || path == null || paramName == null ) {
			LOGGER.warn("One or more parameters null.", usertoken);
			return Response.status(Status.CONFLICT).entity("One or more parameters null.").build();
		}
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ScenarioDefinition sd = ScenarioService.loadScenarioDefinition(scenarioName, usertoken);
		
		if (sd == null) {
			LOGGER.info("No ScenarioDefinition with given name.", usertoken);
			return Response.status(Status.CONFLICT).entity("No ScenarioDefinition with given name.").build();
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = getNamespace(sd.getMeasurementEnvironmentDefinition().getRoot(), path);
		
		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return Response.status(Status.CONFLICT).entity("Namespace does not exist.").build();
		}

		if (!removeNamespace(ns)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot remove namespace.").build();
		}

		if (!ServiceStorageModul.storeScenarioDefition(usertoken, sd)) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot store results in database.").build();
		}

		return Response.ok().build();
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// HELPER /////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Adds all namespaces of the given path if they aren't exist already. The
	 * path will be seperated by the delimiter '/' and ARE be relative to the
	 * root! So the root must be given in the path. E.g.: "/first/second/third"
	 * will be afterwards "root/first/second/third".
	 * 
	 * @param currentNamespace 	the current namespace
	 * @param path 				nodes that will be added
	 * @return 					the last namespace created with the path
	 */
	private ParameterNamespace addNamespaces(ParameterNamespace currentNamespace, String path) {
		LOGGER.info("adding new namespaces '" + path + "'");

		String[] nodes = path.split(ServiceConfiguration.MEASUREMENTENVIRONMENT_DELIMITER);

		if (nodes.length == 0) {
			LOGGER.warn("no namespaces given");
			return null;
		}

		for (int i = 1; i < nodes.length - 1; i++) {
			
			// check if a namespace in the current level with the same name already exists
			boolean found = false;
			
			for (ParameterNamespace ns : currentNamespace.getChildren()) {
				if (ns.getName().equals(nodes[i])) {
					currentNamespace = ns;
					found = true;
					break;
				}
			}

			if (found) {
				continue;
			}

			currentNamespace = addNamespace(nodes[i], currentNamespace);
		}

		currentNamespace = addNamespace(nodes[nodes.length - 1], currentNamespace);
		
		return currentNamespace;
	}

	/**
	 * Adds a new namespace to the target namespace.
	 * 
	 * @param namespaceName 	name of the new namespace
	 * @param targetNamespace 	namespace which gets the new namespace
	 * @return 					the new namespace
	 */
	private ParameterNamespace addNamespace(String namespaceName, ParameterNamespace targetNamespace) {
		LOGGER.info("Adding new namespace '" + namespaceName + "' to parent '" + targetNamespace.getFullName() + "'.");

		// the namespaces are stored in a tree 
		ParameterNamespace newNamespace = SimpleEntityFactory.createNamespace(namespaceName);
		newNamespace.setParent(targetNamespace);
		targetNamespace.getChildren().add(newNamespace);

		return newNamespace;
	}
	
	/**
	 * Returns the namespace specified by the path. The path will be seperated
	 * by the global delimiter (normally '/') and every node representing a namespace.
	 * <br />
	 * <br />
	 * The given path can start with name of the root namespace (default "root",
	 * but don't need to.
	 * 
	 * @param rootNamespace	the root of the {@link ParameterNamespace} of the {@link ScenarioDefinition}
	 * @param path 			path to the namespace
	 * @return 				searched namespace, null if namespace not found
	 */
	private ParameterNamespace getNamespace(ParameterNamespace rootNamespace, String path) {
		String delimiter = ServiceConfiguration.MEASUREMENTENVIRONMENT_DELIMITER;
		
		LOGGER.info("Getting namespace by path '" + path + "'");

		// shorten a beginning "/"
		if (path.length() > 1 && path.substring(0, 1).equals(delimiter)) {
			path = path.substring(1);
		}

		String[] nodes = path.split(delimiter);

		if (nodes.length <= 0) {
			LOGGER.warn("No nodes in path array.");
			return null;
		}

		int startIndex;

		if (nodes.length == 1 && nodes[0].equals(rootNamespace.getName())) {
			LOGGER.debug("Namespace is the root namespace!");
			return rootNamespace;
		} else if (nodes[0].equals(rootNamespace.getName())) {
			startIndex = 1;
		} else {
			startIndex = 0;
		}

		ParameterNamespace currentNamespace = rootNamespace;

		// depth search for the given path in the namespace tree
		for (int i = startIndex; i < nodes.length; i++) {
			
			if (currentNamespace.getChildren().size() <= 0) {
				return null;
			}

			boolean found = false;
			for (ParameterNamespace ns : currentNamespace.getChildren()) {
				if (ns.getName().equals(nodes[i])) {
					currentNamespace = ns;
					found = true;
					break;
				}

			}

			if (found) {
				continue;
			}

			return null;
		}

		LOGGER.info("Found namespace '" + currentNamespace.getFullName() + "'.");

		return currentNamespace;
	}
	
	/**
	 * Removes a namespace and all children from the {@code MeasurementEnvironmentDefinition}.
	 * 
	 * @param namespace namespace which will be removed
	 * @return 			true, if the removal was succesful
	 */
	private boolean removeNamespace(ParameterNamespace namespace) {
		return removeNamespace(namespace, false);
	}
	
	/**
	 * Removes a namespace from the {@code MeasurementEnvironmentDefinition}.
	 * The appended children will be sticked to the parent {@code ParameterNamespace} for the given
	 * namespace.
	 * 
	 * @param namespace namespace which will be removed
	 * @param appendChildrenToParent true, if children namespaces are attached to the parent
	 * @return true, if the removal was succesful
	 */
	private boolean removeNamespace(ParameterNamespace namespace, boolean appendChildrenToParent) {
		LOGGER.debug("removing namespace '" + namespace.getFullName() + "'"
				     + "// appendChildrenToParent: " + appendChildrenToParent);

		if (namespace.getName().equals(ServiceConfiguration.MEASUREMENTENVIRONMENT_ROOTNAME) || namespace.getParent() == null) {
			LOGGER.warn("Operation forbidden: Root namespace can not be removed.");
			return false;
		}

		if (appendChildrenToParent) {
			for (ParameterNamespace child : namespace.getChildren()) {
				child.setParent(namespace.getParent());
				namespace.getParent().getChildren().add(child);
			}
			namespace.getChildren().clear();
		}

		namespace.getParent().getChildren().remove(namespace);

		return true;
	}
	
	/**
	 * Renames the given namespace to the new passed name.
	 * 
	 * @param namespace the namespace to rename
	 * @param newName the new name for the namespace
	 */
	private void renameNamespace(ParameterNamespace namespace, String newName) {
		namespace.setName(newName);
	}
	
	/**
	 * Adding a new parameter to the given namespace.
	 * 
	 * @param name 		name of the new parameter
	 * @param type 		type of the new parameter
	 * @param role 		role of the new parameter
	 * @param namespace namespace where the parameter will be added
	 * @return 			the ParameterDefinition created with the given parameter
	 */
	private ParameterDefinition addParameter(String name, String type, ParameterRole role, ParameterNamespace namespace) {
		if (namespace == null) {
			LOGGER.warn("The given namespace is NULL.");
			return null;
		}
		
		LOGGER.info("Adding new parameter '" + name + "' to namespace '" + namespace.getFullName() + "'.");

		ParameterDefinition newParameter = SimpleEntityFactory.createParameterDefinition(name, type, role);

		newParameter.setNamespace(namespace);
		namespace.getParameters().add(newParameter);

		return newParameter;
	}
	
	/**
	 * Search and return the parameter with the given name. The return type is
	 * the {@code ParameterDefinition}.
	 * 
	 * @param name the parameter name
	 * @param namespace the namespace in which the parameter is searched
	 * @return the {@code ParameterDefinition}, null if search failed
	 */
	public ParameterDefinition getParameter(String name, ParameterNamespace namespace) {
		for (ParameterDefinition parameter : namespace.getParameters()) {
			if (parameter.getName().equals(name)) {
				return parameter;
			}
		}
		return null;
	}
}
