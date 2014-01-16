package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.shared.MECStatus;
import org.sopeco.service.shared.Message;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class MeasurementControllerServiceTest extends JerseyTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementControllerServiceTest.class.getName());
	
	public MeasurementControllerServiceTest() {
		super();
	}

	/**
	 * Configure is called on the object creation of a JerseyTest. It's used to
	 * configure where the JerseyTest can find JSON, the REST service to test
	 * and the JSON POJO.
	 * 
	 * @return the configuration
	 */
	@Override
	public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder(new String[] {
				TestConfiguration.PACKAGE_NAME_JSON,
				TestConfiguration.PACKAGE_NAME_REST })
				.initParam(TestConfiguration.PACKAGE_NAME_POJO, "true")
				.clientConfig(createClientConfig())
				.build();
	}

	/**
	 * Sets the client config for the client to accept JSON and
	 * converting JSON Object to POJOs.
	 * This method is called by {@link configure()}.
	 * 
	 * @return ClientConfig to work with JSON
	 */
	private static ClientConfig createClientConfig() {
		ClientConfig config = new DefaultClientConfig();
	    config.getClasses().add(JacksonJsonProvider.class);
	    // the class contains the configuration to ignore not mappable properties
	    config.getClasses().add(CustomObjectWrapper.class);
	    config.getFeatures().put(TestConfiguration.PACKAGE_NAME_POJO, Boolean.TRUE);
	    return config;
	}
	
	@Test
	public void testMECStatus() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// connect to a random string
		MECStatus mecStatus= resource().path(ServiceConfiguration.SVC_MEC)
									   .path(ServiceConfiguration.SVC_MEC_CHECK)
									   .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
									   .queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
									   .get(MECStatus.class);
		
		assertEquals(MECStatus.NO_VALID_MEC_URL, mecStatus.getStatus());
	
		// check if a wrong token fails, too
		mecStatus= resource().path(ServiceConfiguration.SVC_MEC)
						     .path(ServiceConfiguration.SVC_MEC_CHECK)
						     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, "myrandomtoken")
						     .queryParam(ServiceConfiguration.SVCP_MEC_URL, "random")
						     .get(MECStatus.class);
		
		assertEquals(-1, mecStatus.getStatus());
		
	}
	
	@Test
	public void testBlankMED() {
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		// log into the account
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// create a scenario
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_SWITCH)
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, "examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .put(Boolean.class);
		
		// blank the MeasurementEnvironmentDefinition
		MeasurementEnvironmentDefinition med = resource().path(ServiceConfiguration.SVC_MEC)
														 .path(ServiceConfiguration.SVC_MEC_MED)
														 .path(ServiceConfiguration.SVC_MEC_MED_SET)
														 .path(ServiceConfiguration.SVC_MEC_MED_SET_BLANK)
													     .queryParam(ServiceConfiguration.SVCP_MEC_TOKEN, token)
													     .put(MeasurementEnvironmentDefinition.class);
		
		// as the namespace is not set yet, it must be null
		assertEquals("root", med.getRoot().getName());
	}
	
}
