/**
 * Copyright (c) 2013 SAP
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
package org.sopeco.service.persistence.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.config.IConfiguration;
import org.sopeco.service.builder.ScenarioDefinitionBuilder;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.ServicePersistenceProvider;

/**
 * The User class stores all important information for one user in the database.
 * E.g. the current selected scenario of a user is stored here.
 * 
 * @author Peter Merkert
 */
@Entity
@NamedQuery(name = "getUserByToken", query = "SELECT u FROM Users u WHERE u.token = :token")
public class Users {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Users.class.getName());

	@Id
	@Column(name = "token")
	private String token;
	
	@Lob
	@Column(name = "account")
	private Account currentAccount;
	
	@Lob
	@Column(name = "scenarioDefinitionBuilder")
	private ScenarioDefinitionBuilder currentScenarioDefinitionBuilder;
	
	@Column(name = "currentMeasurementSpecification")
	private String currentMeasurementSpecification;
	
	@Column(name = "lastRequestTime")
	private long lastRequestTime;

	protected Users() {
	}
	
	public Users(String token) {
		this.token = token;
		lastRequestTime = System.currentTimeMillis();
		currentAccount = new Account();
		currentScenarioDefinitionBuilder = new ScenarioDefinitionBuilder();
	}
	
	// ******************************* Setter & Getter ************************************

	public String getToken() {
		return token;
	}
	
	public void setCurrentAccount(Account currentAccount) {
		this.currentAccount = currentAccount;
	}

	public Account getCurrentAccount() {
		return currentAccount;
	}
	
	public ScenarioDefinitionBuilder getCurrentScenarioDefinitionBuilder() {
		return currentScenarioDefinitionBuilder;
	}

	public void setCurrentScenarioDefinitionBuilder(ScenarioDefinitionBuilder scenarioDefinitionBuilder) {
		this.currentScenarioDefinitionBuilder = scenarioDefinitionBuilder;
	}
	
	public long getLastRequestTime() {
		return lastRequestTime;
	}

	public void setLastRequestTime(long pLastRequestTime) {
		this.lastRequestTime = pLastRequestTime;
	}
	
	public String getMeasurementSpecification() {
		return currentMeasurementSpecification;
	}

	public void setMeasurementSpecification(String measurementSpecification) {
		this.currentMeasurementSpecification = measurementSpecification;
	}
	
	// ******************************* Custom methods ************************************

	public AccountDetails getAccountDetails() {
		return ServicePersistenceProvider.getInstance().loadAccountDetails(currentAccount.getId());
	}
	
	public boolean isExpired() {
		LOGGER.debug("Checking user with token '{}' for being expired.", this.toString());
		
		IConfiguration config = Configuration.getSessionSingleton(Configuration.getGlobalSessionId());
		int userTimeout = config.getPropertyAsInteger(ServiceConfiguration.USER_TIMEOUT, 0);
		if (userTimeout == 0 || System.currentTimeMillis() < lastRequestTime + userTimeout) {
			return false;
		} else {
			return true;
		}
	}
	
	public String toString() {
		return 	"_persisted user information_" + "\n"
				+ "token: " + token + "\n"
				+ "last action: " + lastRequestTime + "\n"
				+ "connected to account: " + currentAccount.getName() + "\n";
	}
	
}
