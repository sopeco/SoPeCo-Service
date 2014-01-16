package org.sopeco.service.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

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
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.builder.MeasurementEnvironmentBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.shared.MECStatus;

@Path(ServiceConfiguration.SVC_MEC)
public class MeasurementControllerService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementControllerService.class);
	
	/**
	 * The URL pattern for a controller URL.
	 */
	private static final String[] CONTROLLER_URL_PATTERN = new String[] { "^rmi://[a-zA-Z0-9\\.]+(:[0-9]{1,5})?/[a-zA-Z][a-zA-Z0-9]*$" };

	@GET
	@Path(ServiceConfiguration.SVC_MEC_CHECK)
	@Produces(MediaType.APPLICATION_JSON)
	public MECStatus getMECStatus(@QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken,
								  @QueryParam(ServiceConfiguration.SVCP_MEC_URL) String url) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new MECStatus(-1);
		}

		if (!checkUrlIsValid(url) || url == null) {
			LOGGER.debug("Controller-Status: NO_VALID_MEC_URL");
			return new MECStatus(MECStatus.NO_VALID_MEC_URL);
		}
		
	
		try {
			IMeasurementEnvironmentController meCotnroller = MEConnectorFactory.connectTo(new URI(url));

			MeasurementEnvironmentDefinition med = meCotnroller.getMEDefinition();

			if (med == null) {
				LOGGER.debug("Controller-Status: STATUS_ONLINE_NO_META");
				return new MECStatus(MECStatus.STATUS_ONLINE_NO_META);
			} else {
				LOGGER.debug("Controller-Status: STATUS_ONLINE");
				return new MECStatus(MECStatus.STATUS_ONLINE);
			}

		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException(e);
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException(e);
		} catch (IllegalStateException x) {
			LOGGER.debug("Controller-Status: STATUS_OFFLINE");
			return new MECStatus(MECStatus.STATUS_OFFLINE);
		}
	
	}
	
	@GET
	@Path(ServiceConfiguration.SVC_MEC_VALIDATE)
	@Produces(MediaType.APPLICATION_JSON)
	public String[] getValidUrlPattern() {
		return CONTROLLER_URL_PATTERN;
	}

	@PUT
	@Path(ServiceConfiguration.SVC_MEC_MED + "/"
	      + ServiceConfiguration.SVC_MEC_MED_SET + "/"
	      + ServiceConfiguration.SVC_MEC_MED_SET_URL)
	@Produces(MediaType.APPLICATION_JSON)
	public MeasurementEnvironmentDefinition getMEDefinitionFromMEC(@QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken,
			  													   @QueryParam(ServiceConfiguration.SVCP_MEC_URL) String url) {
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
	@Path(ServiceConfiguration.SVC_MEC_MED + "/"
		  + ServiceConfiguration.SVC_MEC_MED_SET + "/"
		  + ServiceConfiguration.SVC_MEC_MED_SET_BLANK)
	@Produces(MediaType.APPLICATION_JSON)
	public MeasurementEnvironmentDefinition getMEDefinitionFromBlank(@QueryParam(ServiceConfiguration.SVCP_MEC_TOKEN) String usertoken) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);

		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		MeasurementEnvironmentDefinition med = MeasurementEnvironmentBuilder.createBlankEnvironmentDefinition();
		setNewMEDefinition(med, u);
		
		return med;
	}
	
	
	/**************************************HELPER****************************************/
	
	/**
	 * Checks if the given url is like a valid pattern.
	 * 
	 * @param url
	 * @return
	 */
	private boolean checkUrlIsValid(String url) {
		for (String pattern : CONTROLLER_URL_PATTERN) {
			if (url.matches(pattern)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Sets the measurement environment defitinion for the current {@code ScenarioDefinitionBuilder}.
	 * 
	 * @param definition the MED
	 * @param u the user whose MED is to set
	 */
	private void setNewMEDefinition(MeasurementEnvironmentDefinition definition, Users u) {
		LOGGER.debug("Set a new measurement environment definition for the user with token '{}'.", u.getToken());
		
		u.getCurrentScenarioDefinitionBuilder().setMEDefinition(definition);
		ScenarioDefinition sd = u.getCurrentScenarioDefinitionBuilder().getBuiltScenario();
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(u.getToken());
		dbCon.store(sd);
		dbCon.closeProvider();
	}
	
}
