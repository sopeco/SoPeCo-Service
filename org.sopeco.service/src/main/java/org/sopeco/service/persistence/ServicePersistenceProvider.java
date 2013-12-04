package org.sopeco.service.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.exceptions.DataNotFoundException;

import org.sopeco.service.persistence.entities.account.AccountDetails;
import org.sopeco.service.persistence.entities.account.Account;


public final class ServicePersistenceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicePersistenceProvider.class.getName());

	private EntityManagerFactory emf;

	/**
	 * Constructor.
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

	private int updateQuery(String queryName, Object... parameterList) {
		EntityManager em = emf.createEntityManager();
		Query query = em.createNamedQuery(queryName);
		for (int i = 0; i <= parameterList.length / 2; i += 2) {
			query.setParameter((String) parameterList[i], parameterList[i + 1]);
		}
		int count = 0;
		try {
			em.getTransaction().begin();
			count = query.executeUpdate();
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
		return count;
	}
}
