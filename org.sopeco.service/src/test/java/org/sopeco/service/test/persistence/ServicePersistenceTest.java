package org.sopeco.service.test.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sopeco.service.persistence.ServicePersistenceProvider;

/**
 * Tests the service persistence provider.
 * 
 * @author Peter Merkert
 */
public class ServicePersistenceTest {

	/**
	 * If this test fails, then there might be a dependency to an (too) old package
	 * of eclipselink and/or javax. Then this can happen:
	 * {@link https://java.net/projects/jersey/lists/users/archive/2010-03/message/281}
	 * 
	 * For the jersey-test-framework the eclipselink package must the up to date.
	 */
	@Test
	public void persistenceTest() {
		ServicePersistenceProvider spp = ServicePersistenceProvider.getInstance();
		// account with ID 0 does never exist
		assertEquals(null, spp.loadAccount(0));
	}
	
}
