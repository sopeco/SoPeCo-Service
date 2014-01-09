package org.sopeco.service.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.configuration.TestConfiguration;
import org.sopeco.service.shared.Message;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class ScenarioTest extends JerseyTest {

	public ScenarioTest() {
		super();
	}
	
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
	    config.getFeatures().put(TestConfiguration.PACKAGE_NAME_POJO, Boolean.TRUE);
	    return config;
	}
	
	/**
	 * The following test is going to be ignored, because the test statement in the end depends
	 * on the current database state.
	 * If there was no experiement with the given name in the database before, the return value for the
	 * addition of the test is going to be true. Otherwise a scenario with the given name has laready existed
	 * and the result of the REST service is false.
	 * 
	 * The test case is left here to look up default scenario addition.
	 */
	@Ignore("Depend on database state")
	@Test
	public void testScenarioAdd() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		boolean bo = resource().path(ServiceConfiguration.SVC_SCENARIO)
							   .path(ServiceConfiguration.SVC_SCENARIO_ADD)
							   .path("examplescenario")
							   .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
							   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							   .accept(MediaType.APPLICATION_JSON)
							   .type(MediaType.APPLICATION_JSON)
							   .post(Boolean.class, esd);
		
		assertEquals(bo, true);
	}
	

	/*@Test
	public void testScenarioAddScenarioDefintion() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		ScenarioDefinition sd = ScenarioDefinitionBuilder.buildEmptyScenario("test");

		System.out.println("test1");
		
		boolean bo = resource().path(ServiceConfiguration.SVC_SCENARIO)
							   .path(ServiceConfiguration.SVC_SCENARIO_ADD)
							   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							   .accept(MediaType.APPLICATION_JSON)
							   .type(MediaType.APPLICATION_JSON)
							   .post(Boolean.class, sd);
		
		assertEquals(bo, true);
		
	}*/
	
	@Test
	public void testScenarioDoubleAdd() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we check the double adding afterwards)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now add scenario WITH THE SAME NAME a second time
		Boolean b = resource().path(ServiceConfiguration.SVC_SCENARIO)
							  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
							  .path("examplescenario")
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
							  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							  .accept(MediaType.APPLICATION_JSON)
							  .type(MediaType.APPLICATION_JSON)
							  .post(Boolean.class, esd);
		
		assertEquals(b, false);
	}
	
	@Test
	public void testScenarioListing() {
		// connect to test users account
		String accountname = TestConfiguration.TESTACCOUNTNAME;
		String password = TestConfiguration.TESTPASSWORD;
		
		Message m = resource().path(ServiceConfiguration.SVC_ACCOUNT)
							  .path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
							  .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
							  .get(Message.class);
		
		String token = m.getMessage();
		
		// add a default scenario (maybe a scenario with the name already exists, but
		// we don't care about this issue here)
		ExperimentSeriesDefinition esd = new ExperimentSeriesDefinition();
		resource().path(ServiceConfiguration.SVC_SCENARIO)
				  .path(ServiceConfiguration.SVC_SCENARIO_ADD)
				  .path("examplescenario")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, "examplespecname")
				  .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
				  .accept(MediaType.APPLICATION_JSON)
				  .type(MediaType.APPLICATION_JSON)
				  .post(Boolean.class, esd);
		
		// now check if at least one scenario is in the list
		String[] list = resource().path(ServiceConfiguration.SVC_SCENARIO)
							   .path(ServiceConfiguration.SVC_SCENARIO_LIST)
							   .queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, token)
							   .accept(MediaType.APPLICATION_JSON)
							   .post(String[].class);
		
		assertEquals(list.length > 0, true);
	}

	
	
}
