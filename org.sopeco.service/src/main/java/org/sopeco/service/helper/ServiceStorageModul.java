package org.sopeco.service.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.persistence.UserPersistenceProvider;

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
