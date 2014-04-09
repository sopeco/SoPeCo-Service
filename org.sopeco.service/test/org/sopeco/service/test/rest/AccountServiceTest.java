/**
 * Copyright (c) 2014 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
		if (skipTests) return;
		
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
