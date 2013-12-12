package org.sopeco.service.configuration;

/**
 * 
 * @author Peter Merkert
 */
public abstract class ServiceConfiguration {
	
	public static final String USER_TIMEOUT = "sopeco.service.userTimeout";

	public static final String CONFIGURATION_FILE = "sopeco-service.conf";
	
	public static final String SESSION_ID = "SESSION_ID";
	/**
	 * Database configuration key, can be found in the sopeco-service.conf!
	 * So the actual values are in the sopeco-service.conf.
	 */
	public static final String META_DATA_HOST = "sopeco.config.persistence.metaServer.host";
	public static final String META_DATA_PORT = "sopeco.config.persistence.metaServer.port";
	
	public static final String DATABASE_NAME = "sopeco.service.persistence.name";
	public static final String DATABASE_USER = "sopeco.service.persistence.user";
	public static final String DATABASE_PASSWORD = "sopeco.service.persistence.password";

	
}
