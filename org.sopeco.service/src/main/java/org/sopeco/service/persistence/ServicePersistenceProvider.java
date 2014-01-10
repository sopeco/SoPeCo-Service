package org.sopeco.service.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.config.IConfiguration;
import org.sopeco.config.exception.ConfigurationException;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.Users;

/**
 * Visiblity of database modification methods is worldwide. The methods can only be
 * accessed via the class singleton, which can be requested via {@link getInstance()}.
 * 
 * @author Peter Merkert
 */
public final class ServicePersistenceProvider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServicePersistenceProvider.class.getName());

	/**
	 * The entitymanagerfactory is like a thread pol. Entitmanagers to execute database queries
	 * can be created with this factory.
	 * This factory handles a connection pool automatically.
	 * 
	 * @Todo close the emf down at end of program life
	 */
	private EntityManagerFactory emf;

	/**
	 * Singleton instance.
	 */
	private static ServicePersistenceProvider singleton;
	
	/**
	 * Database settings for JDBC
	 */
	private static final String DB_URL = "javax.persistence.jdbc.url";
	private static final String SERVER_URL_PREFIX = "jdbc:derby://";
	private static final String SERVER_URL_SUFFIX = ";create=true";

	/**
	 * Hidden constructor as a contructor for singleton. Get an instance by calling {@link getInstance()}.
	 */
	private ServicePersistenceProvider() {
		
		try {
			emf = Persistence.createEntityManagerFactory("sopeco-service", getConfigOverrides());
		} catch (ConfigurationException ce) {
			LOGGER.warn("Could not load the configuration files");
			LOGGER.warn(ce.getLocalizedMessage());
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage());
			throw new IllegalArgumentException("Could not create persistence provider!", e);
		}
		
	}

	/**
	 * The contructor sets the {@link EntityManagerFactory}. It's used
	 * to load, store and remove items from the database.
	 * 
	 * @param factory the EntityManagerFactory
	 */
	ServicePersistenceProvider(EntityManagerFactory factory) {
		emf = factory;
	}

	public AccountDetails loadAccountDetails(long accountId) {
		return loadSingleById(AccountDetails.class, accountId);
	}

	public List<AccountDetails> loadAllAccountDetails() throws DataNotFoundException {
		return loadByQuery(AccountDetails.class, "getAllAccountDetails");
	}

	public void removeAccountDetails(AccountDetails accountDetails) {
		remove(accountDetails);
	}

	public void storeAccountDetails(AccountDetails accountDetails) {
		store(accountDetails);
	}
	
	public Users loadUser(String token) {
		return loadSingleById(Users.class, token);
	}

	public Users storeUser(Users user) {
		return store(user);
	}

	public void removeUser(Users user) {
		remove(user);
	}

	public Account storeAccount(Account account) {
		return store(account);
	}

	public void removeAccount(Account account) {
		remove(account);
	}

	public Account loadAccount(String accountName) {
		return loadSingleByQuery(Account.class, "getAccountByName", "accountName", accountName);
	}

	public Account loadAccount(long primaryKey) {
		return loadSingleById(Account.class, primaryKey);
	}


	/********************************************************************/
	/**
	 * The methods are taken out of the WebUI from Marius Oehler.
	 * 
	 * @param object the object to store
	 * @return the state of the stored item
	 */
	private <T> T store(T object) {
		EntityManager em = emf.createEntityManager();
		T managedObject = null;
		try {
			em.getTransaction().begin();
			managedObject = em.merge(object);
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
		return managedObject;
	}

	private <T> void remove(T object) {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			T removeObject = em.merge(object);
			em.remove(removeObject);
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	private <T> T loadSingleById(Class<T> returnClazz, Object primaryKey) {
		EntityManager em = emf.createEntityManager();
		T entity = em.find(returnClazz, primaryKey);
		em.close();
		return entity;
	}

	private <T> T loadSingleByQuery(Class<T> returnClazz, String queryName, Object... parameterList) {
		T result = null;
		EntityManager em = emf.createEntityManager();
		try {
			
			TypedQuery<T> query = em.createNamedQuery(queryName, returnClazz);
			for (int i = 0; i <= parameterList.length / 2; i += 2) {
				query.setParameter((String) parameterList[i], parameterList[i + 1]);
			}
			result = query.getSingleResult();
			
		} catch (NoResultException e) {
			
			String parameter = "[";
			for (int i = 0; i < parameterList.length; i++) {
				parameter += (i == 0 ? "" : ", ") + parameterList[i];
			}
			parameter += "]";
			
			LOGGER.debug("No result with query '" + queryName + "' with parameter " + parameter);
		
		} catch (NonUniqueResultException e) {
			
			String parameter = "[";
			for (int i = 0; i < parameterList.length; i++) {
				parameter += (i == 0 ? "" : ", ") + parameterList[i];
			}
			parameter += "]";
			
			LOGGER.debug("No unique result with query '" + queryName + "' with parameter " + parameter);
		
		} catch (IllegalStateException e) {
			LOGGER.error("Query '" + queryName + "' failed: " + e);
		} finally {
			em.close();
		}
		
		return result;
	}

	private <T> List<T> loadByQuery(Class<T> clazz, String queryName, Object... parameterList) {
		List<T> result = new ArrayList<T>();
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<T> query = em.createNamedQuery(queryName, clazz);
			for (int i = 0; i + 1 < parameterList.length; i += 2) {
				query.setParameter((String) parameterList[i], parameterList[i + 1]);
			}
			result = query.getResultList();
		} catch (IllegalStateException e) {
			LOGGER.error("Query '" + queryName + "' failed: " + e);
		} finally {
			em.close();
		}
		return result;
	}
	

	/*******************************Database configuration*************************************/
	
	/**
	 * Creates a new ServicePersistenceProvider to access the database.
	 * 
	 * @return ServicePersistenceProvider to access database
	 */
	public static ServicePersistenceProvider getInstance() {
		
		if (singleton == null) {
			singleton = new ServicePersistenceProvider();
		}
		
		return singleton;
	}

	/**
	 * Creates a configuration map, which contains the connection url to the
	 * database.
	 * 
	 * @return configuration for database
	 */
	private static Map<String, Object> getConfigOverrides() throws ConfigurationException {
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put(DB_URL, getServerUrl());
		LOGGER.debug("Database connection string: {}", configOverrides.get(DB_URL));
		return configOverrides;
	}

	/**
	 * Builds the connection-url of the SoPeCo service database.
	 * 
	 * @return connection-url to the database
	 */
	private static String getServerUrl() throws ConfigurationException {
		
		PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());
		// load the sopeco-service config file
		IConfiguration config = Configuration.getSessionSingleton(Configuration.getGlobalSessionId());
		// sets the root folder where to look for our config files
		config.setAppRootDirectory(ServiceConfiguration.SERVICE_CONFIG_FOLDER);
		config.loadConfiguration(ServiceConfiguration.CONFIGURATION_FILE);
		
		
		if (config.getPropertyAsStr(ServiceConfiguration.META_DATA_HOST) == null) {
			throw new NullPointerException("No MetaDataHost defined.");
		}
		
		String host = config.getPropertyAsStr(ServiceConfiguration.META_DATA_HOST);
		String port = config.getPropertyAsStr(ServiceConfiguration.META_DATA_PORT);
		String name = config.getPropertyAsStr(ServiceConfiguration.DATABASE_NAME);
		String user = config.getPropertyAsStr(ServiceConfiguration.DATABASE_USER);
		String password = config.getPropertyAsStr(ServiceConfiguration.DATABASE_PASSWORD);
		
		return SERVER_URL_PREFIX 	+ host + ":" + port
									+ "/" + name
									+ ";user=" + user
									+ ";password=" + password
									+ SERVER_URL_SUFFIX;
	}
}
