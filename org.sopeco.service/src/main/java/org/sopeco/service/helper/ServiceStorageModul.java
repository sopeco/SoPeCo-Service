package org.sopeco.service.helper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.ScenarioDetails;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.persistence.entities.Users;

/**
 * The {@link ServiceStorageModul} is used to have general complex database storage methods
 * in one class. The methods are needed most times in all the RESTful service classes and
 * can be accessed here in a static way.
 * 
 * @author Peter Merkert
 */
public final class ServiceStorageModul {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStorageModul.class);
	
	/**
	 * Update the {@link AccountDetails} for the account connected to the {@link Users} with the given
	 * token. The selected specification is changed to the one with the given name.<br />
	 * Check: Before the account must have selected a scenario!<br />
	 * Afterwards the {@link AccountDetails} will be stored in the database.<br />
	 * <br />
	 * Only error outputs are created, because here must be no error!
	 * 
	 * @param usertoken				the token to identify the user
	 * @param specificationName		the name of the selected {@link MeasurementSpecification}. This specification
	 * 								is <b>not</b> checked for logical correctness and should match to an
	 * 								available {@link MeasurementSpecification}
	 */
	/*protected static void updateAccountDetailsSelectedSpecification(String usertoken, String specificationname) {

		LOGGER.debug("Trying to update the AccountDetails with a MeasurementSpecification name.");
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.error("The given token is invalid.");
			return;
		}

		AccountDetails ad = u.getAccountDetails();
		
		if (ad == null) {
			LOGGER.error("The user should have selected a scenario before setting the MeasurementSpecification!");
			return;
		}

		String scenarioname = u.getCurrentScenarioDefinitionBuilder().getScenarioDefinition().getScenarioName();
		
		ScenarioDetails sd = ad.getScenarioDetail(scenarioname);
		
		if (sd == null) {
			LOGGER.error("There must be already a ScenarioDetails object in the AccountDetails list with the given scenario name!");
			return;
		}
		
		sd.setSelectedSpecification(specificationname);
		
		ServicePersistenceProvider.getInstance().storeAccountDetails(ad);
	}*/
	
	/**
	 * Update the {@link AccountDetails} for the account connected to the {@link Users} with the given
	 * token. The update only happens, when the name of the scenario in the {@link AccountDetails} match
	 * the name of the scenario in the given {@link ScheduledExperiment}.<br />
	 * The controller URL and the first item of the list of selected experiments is fetched and
	 * updated in the {@link AccountDetails}.<br />
	 * Afterwards the {@link AccountDetails} will be stored in the database.<br />
	 * <br />
	 * Only error outputs are created, because here must be no error!
	 * 
	 * @param usertoken				the token to identify the user
	 * @param scheduledExperiment	the {@link ScheduledExperiment} with all necessary information
	 */
	public static void updateAccountDetails(String usertoken, ScheduledExperiment scheduledExperiment) {
		
		if (scheduledExperiment == null) {
			LOGGER.error("The given ScheduledExperiment is null.");
			return;
		}
		
		LOGGER.debug("Trying to update the AccountDetails with a MeasurementSpecification name.");
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.error("The given token is invalid.");
			return;
		}

		AccountDetails ad = u.getAccountDetails();
		
		if (ad == null) {
			LOGGER.error("The user should have selected a scenario before setting the MeasurementSpecification!");
			return;
		}

		String scenarioname = scheduledExperiment.getScenarioDefinition().getScenarioName();
		
		ScenarioDetails sd = ad.getScenarioDetail(scenarioname);
		
		if (sd == null) {
			LOGGER.error("There must be already a ScenarioDetails object in the AccountDetails list with the given scenario name!");
			return;
		}
		
		List<String> list = scheduledExperiment.getSelectedExperiments();
		
		if (list != null && !list.isEmpty()) {
			sd.setSelectedExperiment(list.get(0)); // TODO only takes the first one
		} else {
			sd.setSelectedExperiment("");
		}

		// get the controller URL via the scheduled experiment
		String controllername = "";
		
		if (scheduledExperiment.getControllerUrl() != null) {
			String[] url = scheduledExperiment.getControllerUrl().split("/");
			if (url != null &&  url.length > 0) {
				controllername = url[url.length-1];
			}
		}
		
		sd.setControllerName(controllername);
		
		if (scheduledExperiment.isActive()) {
			ad.setExperimentKey(scheduledExperiment.getExperimentKey());
		}

		// store the updated AccountDetail in the database
		ServicePersistenceProvider.getInstance().storeAccountDetails(ad);
	}
	
	/**
	 * Update the {@link AccountDetails} for the account connected to the {@link Users} with the given
	 * token. The given {@link ScenarioDefinition} is actually only needed for the scenario name.<br />
	 * The {@link AccountDetails} will have the scenario name as selected scenario name.<br />
	 * Afterwards the {@link AccountDetails} will be stored in the database.
	 * 
	 * @param usertoken							the token to identify the user
	 * @param scenarioDefinition				the {@link ScenarioDefinition}
	 * @param selectedMeasurementSpecification	the name of the selected {@link MeasurementSpecification}
	 */
	public static void updateAccountDetails(String usertoken, ScenarioDefinition scenarioDefinition, String selectedMeasurementSpecification) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Given token '{}' is invalid.", usertoken);
			return;
		}
		
		String scenarioname = scenarioDefinition.getScenarioName();
		AccountDetails ad 	= u.getAccountDetails();
		
		// check if scenario is already in account scenario detail list
		boolean scenarioExists = false;
		for (ScenarioDetails scenarioDetail : ad.getScenarioDetails()) {
			
			if (scenarioDetail.getScenarioName().equals(scenarioname)) {
				// scenario detail is already in list and don't need to be created
				scenarioExists = true;
			}
			
		}
		
		if (!scenarioExists) {
			// must create the ScenarioDetails now
			ScenarioDetails scenarioDetail = new ScenarioDetails();
			scenarioDetail.setScenarioName(scenarioname);
			scenarioDetail.setSelectedSpecification(selectedMeasurementSpecification);
			scenarioDetail.setSelectedExperiment("");
			ad.getScenarioDetails().add(scenarioDetail);
		}

		ad.setSelectedScenario(scenarioname);
		
		ServicePersistenceProvider.getInstance().storeAccountDetails(ad);
	}

	/**
	 * Stores the given {@link ScenarioDefinition} in the users service database.<br />
	 * This method <b>does not</b> check the logical correctness for the
	 * {@link ScenarioDefinition}, moreover it just stores it without even checking null.
	 * 
	 * @param usertoken 			the user identification
	 * @param scenarioDefintion 	the {@link ScenarioDefinition} to store
	 * @return 						true, if the scenario was stored successfully in the database
	 */
	public static boolean storeScenarioDefition(String usertoken, ScenarioDefinition scenarioDefintion) {
		
		IPersistenceProvider dbCon = UserPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found for the user with the token '{}'.", usertoken);
			return false;
		}
		
		dbCon.store(scenarioDefintion);
		dbCon.closeProvider();
		
		return true;
	}
}
