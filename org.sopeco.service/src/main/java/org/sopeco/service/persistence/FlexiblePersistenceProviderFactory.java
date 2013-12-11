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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.PersistenceProviderFactory;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * 
 *
 */
public class FlexiblePersistenceProviderFactory extends PersistenceProviderFactory {
	private static Logger logger = LoggerFactory.getLogger(FlexiblePersistenceProviderFactory.class);

	public static IPersistenceProvider createPersistenceProvider(String host, String port,
			String dbName) {
		return createPersistenceProvider(host, port, dbName, null);
	}

	public static IPersistenceProvider createPersistenceProvider(String host, String port,
			String dbName, String password) {
		if (password != null) {
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).setUsePassword(true);
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBPassword(password);
		} else {
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).setUsePassword(false);
		}
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBHost(host);
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBPort(port);
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBName(dbName);
		Object[] hpn = { host, port, dbName };
		logger.debug("Creating a new persistence provider for {}:{}/{}", hpn);

		FlexiblePersistenceProviderFactory factory = new FlexiblePersistenceProviderFactory();
		return factory.createJPAPersistenceProvider(ServiceConfiguration.SESSION_ID);
	}
}
