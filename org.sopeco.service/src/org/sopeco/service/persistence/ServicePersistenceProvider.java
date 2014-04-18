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
package org.sopeco.service.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.service.execute.MECLogEntry;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
import org.sopeco.service.persistence.entities.MECLog;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
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
	 * can be created with this factory.<br />
	 * This factory handles a connection pool automatically.
	 */
	private EntityManagerFactory emf;

	/**
	 * Singleton instance for this persistence provider.
	 */
	private static ServicePersistenceProvider singleton;

	/**
	 * Hidden constructor as a contructor for singleton. Get an instance by calling {@link getInstance()}.
	 */
	private ServicePersistenceProvider() {
		
		try {
			emf = Persistence.createEntityManagerFactory("sopeco-service");
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
	
	public static void close() {
		
		if (singleton != null) {
			singleton.emf.close();
			singleton = null;
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// DATABASE FETCH METHODS ////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Users loadUser(String token) {
		return loadSingleByQuery(Users.class, "getUserByToken", "token", token);
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
	
	public void storeScheduledExperiment(ScheduledExperiment scheduledExperiment) {
		store(scheduledExperiment);
	}

	public void removeScheduledExperiment(ScheduledExperiment experiment) {
		remove(experiment);
	}
	
	public ScheduledExperiment loadScheduledExperiment(long id) {
		return loadSingleById(ScheduledExperiment.class, id);
	}
	
	public List<ScheduledExperiment> loadAllScheduledExperiments() {
		return loadByQuery(ScheduledExperiment.class, "getAllExperiments");
	}

	public List<ScheduledExperiment> loadScheduledExperimentsByAccount(long accountId) {
		return loadByQuery(ScheduledExperiment.class, "getExperimentsByAccount", "account", accountId);
	}

	public List<ExecutedExperimentDetails> loadExecutedExperimentDetails(long accountId, String scenarioName) {
		return loadByQuery(ExecutedExperimentDetails.class, "getExperiments", "accountId", accountId, "scenarioName", scenarioName);
	}

	/**
	 * Loads an {@link ExecutedExperimentDetails} via the given experiment key.
	 * 
	 * @param experimentKey	the experiment key
	 * @return				the 
	 */
	public ExecutedExperimentDetails loadExecutedExperimentDetails(long experimentKey) {
		LOGGER.debug("Trying to fetch ExecutedExperimentDetails for key '{}' from database.", experimentKey);
		List<ExecutedExperimentDetails> eeds = loadByQuery(ExecutedExperimentDetails.class, "getExperiment", "experimentKey", experimentKey);
		
		if (eeds != null && !eeds.isEmpty()) {
			return eeds.get(0);
		}
		
		return null;
	}
	
	/**
	 * The ID of the {@link MECLog} is the epxerimnet key of the connected experiment.
	 * 
	 * @param experimentkey	the experiment key
	 * @return				the {@link MECLog} with {@link MECLogEntry}s
	 */
	public MECLog loadMECLog(long experimentkey) {
		return loadSingleById(MECLog.class, experimentkey);
	}
	
	public long storeExecutedExperimentDetails(ExecutedExperimentDetails experimentDetails) {
		ExecutedExperimentDetails entity = store(experimentDetails);
		return entity == null ? -1 : entity.getId();
	}
	
	public void storeMECLog(MECLog mecLog) {
		store(mecLog);
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
}
