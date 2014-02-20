package org.sopeco.service.test.configuration;

/**
 * Utility class with the configuration properties for the test environement.
 * 
 * @author Peter Merkert
 */
public abstract class TestConfiguration {

	/**
	 * The Jackson package for JSON converting on RESTful interfaces.
	 */
	public static final String PACKAGE_NAME_JSON = "org.codehaus.jackson.jaxrs";
	
	/**
	 * The Jersey annotation for POJOMappingFeature.
	 */
	public static final String PACKAGE_NAME_POJO = "com.sun.jersey.api.json.POJOMappingFeature";
	
	/**
	 * The package where all the RESTful services are in. Used to setup the {@link JerseyTest}
	 * correctly.
	 */
	public static final String PACKAGE_NAME_REST = "org.sopeco.service.rest";

	/**
	 * The test account, which the test always wants to connect to.
	 */
	public static final String TESTACCOUNTNAME = "testaccount";
	
	/**
	 * The password to access the test account.
	 */
	public static final String TESTPASSWORD = "testpassword";
	
	/**
	 * The default test scenario name. It's used when creating scenarios in test cases.
	 */
	public static final String TEST_SCENARIO_NAME = "testScenarioName";
	
	/**
	 * The default test measurement specification name. It's used when creating the default scenario.
	 */
	public static final String TEST_MEASUREMENT_SPECIFICATION_NAME = "testMeasurementSpecificationName";
	
	/**
	 * The "clearup" scenario is always needed in the database: It's needed to switch to this scenario and
	 * delete the example scenario everytime.
	 */
	public static final String TEST_CLEAN_SCENARIO_NAME = "emptyScenarioName";
	
	/**
	 * The TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME defines the name for the measurement
	 * specification for the "cleanup" scenario.
	 */
	public static final String TEST_CLEAN_MEASUREMENT_SPECIFICATION_NAME = "emptyMeasurementSpecficiationName";
}
