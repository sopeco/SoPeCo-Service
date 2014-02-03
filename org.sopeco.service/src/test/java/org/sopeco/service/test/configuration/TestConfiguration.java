package org.sopeco.service.test.configuration;

/**
 * Utility class with the configuration properties for the test environement.
 * 
 * @author Peter Merkert
 */
public abstract class TestConfiguration {

	public static final String PACKAGE_NAME_JSON = "org.codehaus.jackson.jaxrs";
	public static final String PACKAGE_NAME_POJO = "com.sun.jersey.api.json.POJOMappingFeature";
	public static final String PACKAGE_NAME_REST = "org.sopeco.service.rest";

	public static final String TESTACCOUNTNAME = "testaccount";
	public static final String TESTPASSWORD = "testpassword";
	
	public static final String TEST_SCENARIO_NAME = "testScenarioName";
	
	public static final String TEST_MEASUREMENT_SPECIFICATION_NAME = "testMeasurementSpecficiationName";
	
	/**
	 * The cleared scenario is always in teh database. It's needed to switch to this scenario and
	 * delete the example scenario everytime.
	 */
	public static final String TEST_CLEAN_SCENARIO_NAME = "emptyScenarioName";
	public static final String TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME = "emptyMeasurementSpecficiationName";
}
