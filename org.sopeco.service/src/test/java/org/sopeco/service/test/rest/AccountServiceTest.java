package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The <code>AccountServiceTest</code> tests various features of the
 * <code>AccountService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class AccountServiceTest extends JerseyTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceTest.class.getName());
	
	/**
	 * The default constructor calling the JerseyTest constructor.
	 */
	public AccountServiceTest() {
		super();
	}

	/**
	 * Configure is called on the object creation of a JerseyTest. It's used to
	 * configure where the JerseyTest can find JSON, the REST service to test.
	 * <br />
	 * In addition this method can inject custom ObjectWrapper for Jackson
	 * JSON (un)marshalling.
	 */
	@Override
    protected Application configure() {
		ResourceConfig rc = new ResourceConfig().packages(TestConfiguration.PACKAGE_NAME_REST);
		return rc;
    }
	
	/**
	 * Checks if it is possible to resgister an account twice.
	 */
	@Test
	public void testAccountDoubleCreation() {
		LOGGER.debug("Starting test for double account creation.");
		
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		// just create the account once to be sure it already exists
		target().path(ServiceConfiguration.SVC_ACCOUNT)
			    .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
			    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
			    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
			    .request(MediaType.APPLICATION_JSON_TYPE)
			    .post(Entity.entity(Response.class, MediaType.APPLICATION_JSON));
		
		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
				             .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
				             .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
				             .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
				             .request(MediaType.APPLICATION_JSON_TYPE)
				             .post(Entity.entity(Response.class, MediaType.APPLICATION_JSON));

		assertEquals(true, Status.CONFLICT.getStatusCode() == r.getStatus());
	}

	/**
	 * Checks it the account with the name already exists (after creating it).
	 */
	@Test
	public void testCheckAccountExistence() {
		String accountname 	= TestConfiguration.TESTACCOUNTNAME;
		String password 	= TestConfiguration.TESTPASSWORD;
		
		// the creation might fail, but we are only interested if afterwards at least one user
		// with username "testuser" already exists
		target().path(ServiceConfiguration.SVC_ACCOUNT)
			    .path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
			    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
			    .queryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD, password)
			    .request(MediaType.APPLICATION_JSON_TYPE)
			    .post(Entity.entity(Response.class, MediaType.APPLICATION_JSON));

		Response r = target().path(ServiceConfiguration.SVC_ACCOUNT)
						     .path(ServiceConfiguration.SVC_ACCOUNT_EXISTS)
						     .queryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME, accountname)
						     .request(MediaType.APPLICATION_JSON_TYPE)
						     .get(Response.class);

		assertEquals(true, Status.OK.getStatusCode() == r.getStatus());
		assertEquals(true, r.readEntity(Boolean.class));
	}
	
}
