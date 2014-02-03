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
}
