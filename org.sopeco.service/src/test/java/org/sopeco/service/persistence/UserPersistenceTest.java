package org.sopeco.service.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.service.persistence.entities.account.Account;

/**
 * Tests the service persistence provider.
 * 
 * @author Peter Merkert
 */
public class UserPersistenceTest {

	/**
	 * If this test fails, then there might be a dependency to an (too) old package
	 * of eclipselink and/or javax. Then this can happen:
	 * {@link https://java.net/projects/jersey/lists/users/archive/2010-03/message/281}
	 * 
	 * For the jersey-test-framework the eclipselink package must the up to date.
	 */
	@Test
	public void PersistenceTest() {
		// test for error checking
		IPersistenceProvider pp = UserPersistenceProvider.createPersistenceProvider((Account)null);
		assertEquals(null, pp);
		
	}
	
}
