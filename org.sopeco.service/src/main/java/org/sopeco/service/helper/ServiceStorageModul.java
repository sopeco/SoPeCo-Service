/**
 * Copyright (c) 2014 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.service.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.persistence.AccountPersistenceProvider;

/**
 * The {@link ServiceStorageModul} is used to have general database access methods
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
		
		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(usertoken);
		
		if (dbCon == null) {
			LOGGER.warn("No database connection found for the user with the token '{}'.", usertoken);
			return false;
		}
		
		dbCon.store(scenarioDefintion);
		dbCon.closeProvider();
		
		return true;
	}
	
	/**
	 * Load a Scenario definition with the given name and user (via token).
	 * 
	 * @param scenarioname 	the name of the scenario which definition has to be loaded
	 * @param token 		the token to identify the user
	 * @return 				The scenario definition for the scenario with the given name.
	 * 						Null if there is no scenario with the given name.
	 */
	public static ScenarioDefinition loadScenarioDefinition(String scenarioname, String token) {

		IPersistenceProvider dbCon = AccountPersistenceProvider.createPersistenceProvider(token);
		
		try {
			
			ScenarioDefinition definition = dbCon.loadScenarioDefinition(scenarioname);
			return definition;
			
		} catch (DataNotFoundException e) {
			
			LOGGER.warn("Scenario '{}' not found.", scenarioname);
			return null;
		
		} finally {
			dbCon.closeProvider();
		}
		
	}
	
	/**
	 * Loads the {@link ScenarioDefinition} with the given name out of the database. The {@link MeasurementSpecification}
	 * with the given name is fetched from the loaded {@link ScenarioDefinition} and returned. Of coure, <code>null</code>
	 * can be returned.
	 * 
	 * @param scenarioName	the name of the {@link ScenarioDefinition}
	 * @param measSpecName	the name of the {@link MeasurementSpecification}
	 * @param token			the user identification
	 * @return				the {@link MeasurementSpecification}, <code>null</code> possible
	 */
	public static MeasurementSpecification loadMeasurementSpecification(String scenarioName, String measSpecName, String token) {

		ScenarioDefinition sd = loadScenarioDefinition(scenarioName, token);
		
		if (sd == null) {
			LOGGER.info("Cannot find a ScenarioDefition with the given name in the database.");
			return null;
		}
		
		return sd.getMeasurementSpecification(measSpecName);
	}
}
