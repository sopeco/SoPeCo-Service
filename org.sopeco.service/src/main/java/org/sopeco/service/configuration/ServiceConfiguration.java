package org.sopeco.service.configuration;

import java.util.concurrent.TimeUnit;

/**
 * The default service configuration. Here you can find all the URI for the RESTful service and
 * a lot of configuration lookup properties of the SoPeCo Configuration.
 * 
 * @author Peter Merkert
 */
public abstract class ServiceConfiguration {
	
	public static final String USER_TIMEOUT = "sopeco.service.userTimeout";
	
	public static final String CONFIGURATION_FILE = "sopeco-service.conf";

	public static final String SESSION_ID = "sessionid";
	
	public static final String SERVICE_CONFIG_FOLDER = "rsc";
	
	/**
	 * Used in class {@code ServerCheck}.
	 */
	public static final int SOCKET_TEST_TIMEOUT_MS = 10000;

	// timeunit for the scheduling times can be configured below them
	public static final int SCHEDULING_REPEATE_INTERVAL = 5;
	public static final int SCHEDULING_TIMEOUT_INTERVAL = 5; // waits for the experiment scheduler to finish
	public static final TimeUnit SCHEDULING_TIME_UNIT = TimeUnit.SECONDS; // in seconds
	
	public static final String DEFAULT_MEASUREMENTSPECIFICATION_NAME = "MeasurementSpecification";
	
	public static final String MEASUREMENTENVIRONMENT_ROOTNAME = "root";
	public static final String MEASUREMENTENVIRONMENT_DELIMITER = "/";
	
	// Database configuration
	public static final String DATABASE_HOST = "sopeco.service.persistence.host";
	public static final String DATABASE_PORT = "sopeco.service.persistence.port";
	public static final String DATABASE_NAME = "sopeco.service.persistence.name";
	public static final String DATABASE_USER = "sopeco.service.persistence.user";
	public static final String DATABASE_PASSWORD = "sopeco.service.persistence.password";

	// MEC connection configuration
	public static final String MEC_PORT = "sopeco.config.mec.listener.port";
	public static final int MEC_SOCKET_PORT = 8089;
	public static final String MEC_SOCKET_HOST = "127.0.0.1";
	
	/**
	 * All the REST services are listed down here.
	 * All params with prefix SVC points to paths.
	 * All params with prefix SVCP points to parameter names.
	 * 
	 * account
	 * 		- create (username/pw)
	 * 		- exists (username)
	 * 		- login (username/pw)
	 * 
	 * info
	 * 
	 * scenario
	 * 		- add
	 * 		- list
	 * 		- delete
	 * 		- switch
	 * 		- current
	 * 		- store
	 * 		- xml
	 * 
	 * measurementspecification
	 * 		- list
	 * 		- listspecifications (as objects)
	 * 		- switch
	 * 		- create
	 * 		- rename (current measuremtnspecification)
	 * 
	 * measurementcontroller
	 * 		- status (of MEC)
	 * 		- validate
	 * 		- med (measurment environment definition)
	 * 			- blank
	 * 		- current (MED)
	 * 		- check (if Port is reachable)
	 * 
	 * measurementenviromentdefintion (MED)
	 * 		- med
	 * 			- blank
	 * 		- current (MED)
	 * 		- namespace
	 * 			- add
	 * 			- remove
	 * 			- rename
	 * 		- parameter
	 * 			- add
	 * 			- remove
	 * 			- update
	 * 
	 * execute
	 * 		- schedule (POST, GET, REMOVE)
	 * 	 	- details
	 */
	public static final String SVC_ACCOUNT = "account";
	public static final String SVC_ACCOUNT_CHECK = "check";
	public static final String SVC_ACCOUNT_CREATE = "create";
	public static final String SVC_ACCOUNT_EXISTS = "exists";
	public static final String SVC_ACCOUNT_LOGIN = "login";
	public static final String SVC_ACCOUNT_INFO = "info";
	public static final String SVC_ACCOUNT_TOKEN = "token";
	public static final String SVC_ACCOUNT_CONNECTED = "connected";
	public static final String SVC_ACCOUNT_CUSTOMIZE = "customize";
	public static final String SVCP_ACCOUNT_NAME = "accountname";
	public static final String SVCP_ACCOUNT_PASSWORD = "password";
	public static final String SVCP_ACCOUNT_TOKEN = "token";
	public static final String SVCP_ACCOUNT_DATABASENAME = "dbname";
	public static final String SVCP_ACCOUNT_DATABASEPORT = "dbpw";

	public static final String SVC_INFO = "info";
	public static final String SVC_INFO_RUNNING = "running";
	
	public static final String SVC_SCENARIO = "scenario";
	public static final String SVC_SCENARIO_ADD = "add";
	public static final String SVC_SCENARIO_LIST = "list";
	public static final String SVC_SCENARIO_DELETE = "delete";
	public static final String SVC_SCENARIO_SWITCH = "switch";
	public static final String SVC_SCENARIO_SWITCH_NAME = "name";
	public static final String SVC_SCENARIO_SWITCH_DEFINITION = "definition";
	public static final String SVC_SCENARIO_CURRENT = "current";
	public static final String SVC_SCENARIO_ARCHIVE = "archive";
	public static final String SVC_SCENARIO_XML = "xml";
	public static final String SVC_SCENARIO_INSTANCE = "instance";
	public static final String SVCP_SCENARIO_NAME = "name";
	public static final String SVCP_SCENARIO_SPECNAME = "specname";
	public static final String SVCP_SCENARIO_TOKEN = "token";
	public static final String SVCP_SCENARIO_URL = "url";
	
	public static final String SVC_MEASUREMENT = "measurementspecification";
	public static final String SVC_MEASUREMENT_LIST = "list";
	public static final String SVC_MEASUREMENT_LISTSPECS = "listspecification";
	public static final String SVC_MEASUREMENT_SWITCH = "switch";
	public static final String SVC_MEASUREMENT_CREATE = "create";
	public static final String SVC_MEASUREMENT_RENAME = "rename";
	public static final String SVCP_MEASUREMENT_TOKEN = "token";
	public static final String SVCP_MEASUREMENT_NAME = "specname";
	
	public static final String SVC_MEC = "mec";
	public static final String SVC_MEC_STATUS = "status";
	public static final String SVC_MEC_VALIDATE = "validate";
	public static final String SVC_MEC_CHECK = "check";
	public static final String SVC_MEC_LIST = "list";
	public static final String SVC_MEC_MED = "med";
	public static final String SVCP_MEC_TOKEN = "token";
	public static final String SVCP_MEC_URL = "url";
	public static final String SVCP_MEC_HOST = "host";
	public static final String SVCP_MEC_PORT = "port";
	public static final String SVCP_MEC_ID = "id";
	
	public static final String SVC_MED = "med";
	public static final String SVC_MED_SET = "set";
	public static final String SVC_MED_SET_MEC = "mec";
	public static final String SVC_MED_SET_BLANK = "blank";
	public static final String SVC_MED_CURRENT = "current";
	public static final String SVC_MED_NAMESPACE = "namespace";
	public static final String SVC_MED_NAMESPACE_ADD = "add";
	public static final String SVC_MED_NAMESPACE_REMOVE = "remove";
	public static final String SVC_MED_NAMESPACE_RENAME = "rename";
	public static final String SVC_MED_PARAM = "parameter";
	public static final String SVC_MED_PARAM_ADD = "add";
	public static final String SVC_MED_PARAM_REMOVE = "remove";
	public static final String SVC_MED_PARAM_UPDATE = "update";
	public static final String SVCP_MED_TOKEN = "token";
	public static final String SVCP_MED_MEC_URL = "url";
	public static final String SVCP_MED_NAMESPACE = "path"; // namespace path
	public static final String SVCP_MED_NAMESPACE_NEW = "newName"; // updated namespace name
	public static final String SVCP_MED_PARAM_NAME = "paramname";
	public static final String SVCP_MED_PARAM_NAME_NEW = "newparamname";
	public static final String SVCP_MED_PARAM_TYP = "paramtype";

	public static final String SVC_EXECUTE = "execution";
	public static final String SVC_EXECUTE_ESD = "esd";
	public static final String SVC_EXECUTE_SCHEDULE = "schedule";
	public static final String SVC_EXECUTE_ENABLE = "enable";
	public static final String SVC_EXECUTE_DISABLE = "disable";
	public static final String SVC_EXECUTE_ID = "id";
	public static final String SVC_EXECUTE_DETAILS = "details";
	public static final String SVC_EXECUTE_MECLOG = "meclog";
	public static final String SVC_EXECUTE_EXECUTE = "execute";
	public static final String SVC_EXECUTE_STATUS = "status";
	public static final String SVCP_EXECUTE_TOKEN = "token";
	public static final String SVCP_EXECUTE_ID = "id";
	public static final String SVCP_EXECUTE_EXPERIMENTSERIES = "experimentseriesname";
	public static final String SVCP_EXECUTE_KEY = "key";
	
	/**
	 * QueryParam name for the account password.
	 */
	public static final String SVC_ACCOUNT_PASSWORD = "password";
	public static final String SVC_USER = "user";
	public static final String SVC_DB_PREFIX = "SPC_SVC";





}
