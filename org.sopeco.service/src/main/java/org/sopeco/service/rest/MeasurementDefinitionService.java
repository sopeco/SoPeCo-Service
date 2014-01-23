package org.sopeco.service.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.IMeasurementEnvironmentController;
import org.sopeco.engine.measurementenvironment.connector.MEConnectorFactory;
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

@Path(ServiceConfiguration.SVC_MED)
public class MeasurementDefinitionService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementDefinitionService.class);

	private static final String TOKEN = ServiceConfiguration.SVCP_MED_TOKEN;
	
	@PUT
	@Path(ServiceConfiguration.SVC_MED_SET + "/"
	      + ServiceConfiguration.SVC_MED_SET_MEC)
	@Produces(MediaType.APPLICATION_JSON)
	public MeasurementEnvironmentDefinition setMEDefinitionFromMEC(@QueryParam(TOKEN) String usertoken,
			  													   @QueryParam(ServiceConfiguration.SVCP_MED_MEC_URL) String url) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		try {
			IMeasurementEnvironmentController mec = MEConnectorFactory.connectTo(new URI(url));

			MeasurementEnvironmentDefinition med = mec.getMEDefinition();

			setNewMEDefinition(med, u);

			return med;
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException(e);
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException(e);
		}
	}
	
	@PUT
	@Path(ServiceConfiguration.SVC_MED_SET + "/"
		  + ServiceConfiguration.SVC_MED_SET_BLANK)
	@Produces(MediaType.APPLICATION_JSON)
	public MeasurementEnvironmentDefinition getMEDefinitionFromBlank(@QueryParam(TOKEN) String usertoken) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		MeasurementEnvironmentDefinition med = MeasurementEnvironmentDefinitionBuilder.createBlankEnvironmentDefinition();
		setNewMEDefinition(med, u);
		
		return med;
	}
	
	/**
	 * Returns current selected MED for the given user.
	 * 
	 * @param usertoken the user identification
	 * @return current selected MED for the given user
	 */
	@GET
	@Path(ServiceConfiguration.SVC_MED_CURRENT)
	@Produces(MediaType.APPLICATION_JSON)
	public MeasurementEnvironmentDefinition getCurrentMEDefinition(@QueryParam(TOKEN) String usertoken) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
	
		return u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentDefinition();
	}
	
	@PUT
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean addNamespace(@QueryParam(TOKEN) String usertoken,
								@QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .addNamespaces(path);

		if (ns == null) {
			return false;
		}
		
		storeUserAndScenario(u);

		return true;
	}
	
	
	@DELETE
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeNamespace(@QueryParam(TOKEN) String usertoken,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return false;
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .getNamespace(path);

		if (ns == null) {
			LOGGER.warn("Namespace with the path '{}' does not exist!", path);
			return false;
		}
		
		u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentBuilder().removeNamespace(ns);
		
		// store the updated scenario with removed namespace in database
		storeUserAndScenario(u);

		return true;
	}
	
	
	@PUT
	@Path(ServiceConfiguration.SVC_MED_NAMESPACE + "/"
			+ ServiceConfiguration.SVC_MED_NAMESPACE_RENAME)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean renameNamespace(@QueryParam(TOKEN) String usertoken,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
								   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE_NEW) String newName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
							 	 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return false;
		}
		
		u.getCurrentScenarioDefinitionBuilder().getMeasurementEnvironmentBuilder().renameNamespace(ns, newName);
		
		storeUserAndScenario(u);

		return true;
	}
	
	@PUT
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_ADD)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean addParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				@QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				@QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				@QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				ParameterRole role) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return false;
		}

		u.getCurrentScenarioDefinitionBuilder()
		 .getMeasurementEnvironmentBuilder()
		 .addParameter(paramName, paramType, role, ns);

		storeUserAndScenario(u);

		return true;
	}
	
	@PUT
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_UPDATE)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean updateParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME_NEW) String paramNameNew,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_TYP) String paramType,
			      				   ParameterRole role) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return false;
		}

		ParameterDefinition parameter = u.getCurrentScenarioDefinitionBuilder()
										 .getMeasurementEnvironmentBuilder()
										 .getParameter(paramName, ns);
		
		if (parameter == null) {
			LOGGER.info("Parameter '{}' does not exist in the namespace with path '{}'!", paramName, path);
			return false;
		}
		
		parameter.setName(paramNameNew);
		parameter.setRole(role);
		parameter.setType(paramType);

		storeUserAndScenario(u);

		return true;
	}
	
	@DELETE
	@Path(ServiceConfiguration.SVC_MED_PARAM + "/"
			+ ServiceConfiguration.SVC_MED_PARAM_REMOVE)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeParameter(@QueryParam(ServiceConfiguration.SVCP_MED_TOKEN) String usertoken,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_NAMESPACE) String path,
			      				   @QueryParam(ServiceConfiguration.SVCP_MED_PARAM_NAME) String paramName) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.info("Invalid token '{}'!", usertoken);
			return false;
		}
		
		LOGGER.debug("Try to add parameter with name '{}' to path '{}'", paramName, path);

		ParameterNamespace ns = u.getCurrentScenarioDefinitionBuilder()
								 .getMeasurementEnvironmentBuilder()
								 .getNamespace(path);

		if (ns == null) {
			LOGGER.info("Namespace with the path '{}' does not exist!", path);
			return false;
		}
		
		boolean b = u.getCurrentScenarioDefinitionBuilder()
					 .getMeasurementEnvironmentBuilder()
					 .removeNamespace(ns);

		storeUserAndScenario(u);

		return b;
	}
	
	/**************************************HELPER****************************************/
	
	/**
	 * Stores the current user state in the service database. The current scenario state for the given
	 * user is stored in the connected account database.
	 * 
	 * @param u the user whose information should be stored
	 */
	private void storeUserAndScenario(Users u) {
		// store scenario in account database
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(u.getToken());
		
		if (dbCon == null) {
			LOGGER.warn("Cannot open the account database. Given token is '{}'", u.getToken());
			return;
		}
		
		dbCon.store(sd);
		dbCon.closeProvider();

		// store user information in Service-database
		ServicePersistenceProvider.getInstance().storeUser(u);
	}
	
	/**
	 * Sets the measurement environment defitinion for the current {@code ScenarioDefinitionBuilder}.
	 * 
	 * @param definition the MED
	 * @param u the user whose MED is to set
	 */
	private void setNewMEDefinition(MeasurementEnvironmentDefinition definition, Users u) {
		LOGGER.debug("Set a new measurement environment definition for the user with token '{}'.", u.getToken());
		
		u.getCurrentScenarioDefinitionBuilder().setMeasurementEnvironmentDefinition(definition);
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition();
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(u.getToken());
		dbCon.store(sd);
		dbCon.closeProvider();
	}
	
}
