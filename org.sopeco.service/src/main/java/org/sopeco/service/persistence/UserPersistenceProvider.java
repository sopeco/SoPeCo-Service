package org.sopeco.service.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.PersistenceProviderFactory;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.account.Account;
import org.sopeco.service.user.User;
import org.sopeco.service.user.UserManager;

public class UserPersistenceProvider extends PersistenceProviderFactory {
	
	private static Logger LOGGER = LoggerFactory.getLogger(UserPersistenceProvider.class.getName());

	/**
	 * Create the database connection for the given user (via token). The passed token
	 * identifies a unique user in for the currently conected users.
	 * 
	 * @param token the token to identify the user
	 * @return persistence provider for the given user (via token)
	 */
	public static IPersistenceProvider createPersistenceProvider(String token) {

		User u = UserManager.instance().getUser(token);

		Account account = u.getCurrentAccount();
		
		System.out.println(account.toString());
		
		return createPersistenceProvider(account);
	}
	
	/**
	 * Create the database connection for the given account.
	 * 
	 * @param account the account whose database connection is requested
	 * @return persistence provider for the given account
	 */
	public static IPersistenceProvider createPersistenceProvider(Account account) {
		
		String dbPassword = "testpassword";//account.getDbPassword();
		String host 	  = account.getDbHost();
		String port 	  = Integer.toString(account.getDbPort());
		String name 	  = account.getDbName();
		
		System.out.println(dbPassword);
		System.out.println(host);
		System.out.println(port);
		System.out.println(name);
		
		if (dbPassword.isEmpty()) {
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).setUsePassword(false);
		} else {
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).setUsePassword(true);
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBPassword(dbPassword);
		}
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBHost(host);
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBPort(port);
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBName(name);
		
		Object[] hpn = { host, port, name };
		LOGGER.debug("Creating a new persistence provider for {}:{}/{}", hpn);

		UserPersistenceProvider factory = new UserPersistenceProvider();
		return factory.createJPAPersistenceProvider(ServiceConfiguration.SESSION_ID);
	}
	
}
