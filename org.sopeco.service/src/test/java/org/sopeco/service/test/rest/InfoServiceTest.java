package org.sopeco.service.test.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * The <code>InfoServiceTest</code> tests various features of the
 * <code>InfoService</code> RESTful services.
 * 
 * @author Peter Merkert
 */
public class InfoServiceTest extends AbstractServiceTest {

	/**
	 * Tests the default service request interface, if the service
	 * is running.
	 */
	@Test
	public void testServiceRunning() {
		Response r = target().path(ServiceConfiguration.SVC_INFO)
							 .path(ServiceConfiguration.SVC_INFO_RUNNING)
							 .request(MediaType.APPLICATION_JSON)
							 .get();

		assertEquals(Status.OK.getStatusCode(), r.getStatus());
	}
	
}
