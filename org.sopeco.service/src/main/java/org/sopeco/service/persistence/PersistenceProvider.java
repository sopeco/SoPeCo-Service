package org.sopeco.service.persistence;

import java.util.logging.Logger;


public final class PersistenceProvider {

	private static final Logger LOGGER = Logger.getLogger(PersistenceProvider.class.getName());

	private static final String DB_URL = "javax.persistence.jdbc.url";
	private static final String SERVER_URL_PREFIX = "jdbc:derby://";
	private static final String SERVER_URL_SUFFIX = ";create=true";
	private static final String USER_TIMEOUT = "sopeco.ui.userTimeout";
	private static final String TIMEOUT_CHECK_INTERVAL = "sopeco.ui.timeoutCheckInterval";
	private static final String META_DATA_HOST = "sopeco.config.persistence.metaServer.host";
	private static final String META_DATA_PORT = "sopeco.config.persistence.metaServer.port";
	private static final String SOPECO_UI_DATABASE_NAME = "sopeco.ui.persistence.name";
	private static final String SOPECO_UI_DATABASE_USER = "sopeco.ui.persistence.user";
	private static final String SOPECO_UI_DATABASE_PASSWORD = "sopeco.ui.persistence.password";
	private static final String SOPECO_UI_USERTIMEOUT = "sopeco.ui.userTimeout";
	private static final String SOPECO_CONFIG_MEC_LISTENER_PORT = "sopeco.config.mec.listener.port";
	
	private PersistenceProvider() {
	}
	
}
