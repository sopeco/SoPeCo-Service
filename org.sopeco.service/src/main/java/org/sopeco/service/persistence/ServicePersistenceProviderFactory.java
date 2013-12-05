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
package org.sopeco.service.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.sopeco.config.Configuration;
import org.sopeco.config.IConfiguration;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * 
 * @author Marius Oehler
 * 
 */
public final class ServicePersistenceProviderFactory {

	private static final Logger LOGGER = Logger.getLogger(ServicePersistenceProviderFactory.class.getName());
	
	/**
	 * Database settings for JDBC
	 */
	public static final String DB_URL = "javax.persistence.jdbc.url";
	public static final String SERVER_URL_PREFIX = "jdbc:derby://";
	public static final String SERVER_URL_SUFFIX = ";create=true";

	/**
	 * Hidden constructor as a contructor is available in a static way
	 * called {@link ServicePersistenceProviderFactory.createServicePersistenceProvider}.
	 */
	private ServicePersistenceProviderFactory() {
	}

	/**
	 * Creates a new ServicePersistenceProvider to access the database.
	 * 
	 * @return ServicePersistenceProvider to access database
	 */
	public static ServicePersistenceProvider createServicePersistenceProvider() {
		try {
			EntityManagerFactory factory = Persistence.createEntityManagerFactory("sopeco-service", getConfigOverrides());
			return new ServicePersistenceProvider(factory);
		} catch (Exception e) {
			LOGGER.severe(e.getLocalizedMessage());
			throw new IllegalArgumentException("Could not create persistence provider!", e);
		}
	}

	/**
	 * Creates a configuration map, which contains the connection url to the
	 * database.
	 * 
	 * @return configuration for database
	 */
	private static Map<String, Object> getConfigOverrides() {
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put(DB_URL, getServerUrl());
		return configOverrides;
	}

	/**
	 * Builds the connection-url of the SoPeCo service database.
	 * 
	 * @return connection-url to the database
	 */
	private static String getServerUrl() {
		
		PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());
		IConfiguration config = Configuration.getSessionSingleton(Configuration.getGlobalSessionId());
		
		if (config.getPropertyAsStr(ServiceConfiguration.META_DATA_HOST) == null) {
			throw new NullPointerException("No MetaDataHost defined.");
		}
		
		String host = config.getPropertyAsStr(ServiceConfiguration.META_DATA_HOST);
		String port = config.getPropertyAsStr(ServiceConfiguration.META_DATA_PORT);
		String name = config.getPropertyAsStr(ServiceConfiguration.DATABASE_NAME);
		String user = config.getPropertyAsStr(ServiceConfiguration.DATABASE_USER);
		String password = config.getPropertyAsStr(ServiceConfiguration.DATABASE_PASSWORD);
		
		return SERVER_URL_PREFIX 	+ host + ":" + port
									+ "/" + name
									+ ";user=" + user
									+ ";password=" + password
									+ SERVER_URL_SUFFIX;
	}
}
