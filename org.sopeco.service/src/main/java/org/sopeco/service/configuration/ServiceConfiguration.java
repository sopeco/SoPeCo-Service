package org.sopeco.service.configuration;

/**
 * 
 * @author Peter Merkert
 */
public abstract class ServiceConfiguration {
	
	public static final String USER_TIMEOUT = "sopeco.service.userTimeout";
	
	public static final String TIMEOUT_CHECK_INTERVAL = "sopeco.service.timeoutCheckInterval";
	
	/**
	 * Database configuration
	 */
	public static final String META_DATA_HOST = "sopeco.config.persistence.metaServer.host";
	public static final String META_DATA_PORT = "sopeco.config.persistence.metaServer.port";
	public static final String DATABASE_NAME = "org.sopeco.service.name";
	public static final String DATABASE_USER = "org.sopeco.service.user";
	public static final String DATABASE_PASSWORD = "org.sopeco.service.password";

	public static final String SESSION_ID = "SESSION_ID";
	
}
