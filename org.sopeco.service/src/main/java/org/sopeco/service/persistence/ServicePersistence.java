package org.sopeco.service.persistence;

import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.ServicePersistenceProviderFactory;

/**
 * Creates and instance for the {@link ServicePersistenceProvier} to access the database.
 * It's access in a singleton way.
 * 
 * @author Peter Merkert
 */
public class ServicePersistence {

	/**
	 * Singleton instance for the service persistence provider.
	 */
	private static ServicePersistenceProvider sPersistenceProvider;
	
	/**
	 * Hidden constructor as the class is only used for handling singletons.
	 */
	private ServicePersistence() {
	}

	/**
	 * Returns the persistence providor to access the database. The database
	 * contains all information about users and experiements.
	 * 
	 * @return ServicePersistenceProvider to access database
	 */
	public static ServicePersistenceProvider getServicePersistenceProvider() {
		if (sPersistenceProvider == null) {
			sPersistenceProvider = ServicePersistenceProviderFactory.createServicePersistenceProvider();
		}
		return sPersistenceProvider;
	}
	
}
