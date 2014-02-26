package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.rest.AccountService;
import org.sopeco.service.test.configuration.TestConfiguration;

/**
 * The <code>AccountServiceTest</code> tests various features of the
 * {@link AccountService} RESTful services.
 * 
 * @author Peter Merkert
 */
public class AccountServiceTest extends AbstractServiceTest {

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
