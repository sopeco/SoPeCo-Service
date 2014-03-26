package org.sopeco.service.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.PersistenceProviderFactory;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.Users;

public class UserPersistenceProvider extends PersistenceProviderFactory {

	private static Logger LOGGER = LoggerFactory.getLogger(UserPersistenceProvider.class);
	
	/**
	 * Create the database connection for the given user (via token). The passed token
	 * identifies a unique user in for the currently conected users.
	 * 
	 * @param token the token to identify the user
	 * @return persistence provider for the given user (via token)
	 */
	public static IPersistenceProvider createPersistenceProvider(String token) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(token);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", token);
			return null;
		}
		
		Account account = ServicePersistenceProvider.getInstance().loadAccount(u.getAccountID());
		
		return createPersistenceProvider(account);
	}
	
	/**
	 * Create the database connection for the given account.
	 * 
	 * @param account the account whose database connection is requested
	 * @return persistence provider for the given account
	 */
	public static IPersistenceProvider createPersistenceProvider(Account account) {
		
		if (account == null) {
			LOGGER.warn("Given account is invalid!");
			return null;
		}
		
		String dbPassword  = account.getDbPassword();
		String host 	   = account.getDbHost();
		String port 	   = Integer.toString(account.getDbPort());
		String name 	   = account.getDbName();
		
		if (dbPassword.isEmpty()) {
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).setUsePassword(false);
		} else {
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).setUsePassword(true);
			PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBPassword(dbPassword);
		}
		
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBHost(host);
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBPort(port);
		PersistenceConfiguration.getSessionSingleton(ServiceConfiguration.SESSION_ID).updateDBName(name);

		LOGGER.debug("Creating a new persistence provider for account '{}'", account.getName());
		
		UserPersistenceProvider factory = new UserPersistenceProvider();
		return factory.createJPAPersistenceProvider(ServiceConfiguration.SESSION_ID);
	}
	
	/**
	 * Updates the persistence configuration properties for the persistence provider for the
	 * given token. One configuration is connected to a unique token. The token is here the one
	 * from the user. The user is connected to an account. The account database connection
	 * properties are stores in the configuration.<br />
	 * <br />
	 * This method is primarily used to once the user logs into an account. The configuration is
	 * updated with the account database settings. See service <i>LOGIN</i> at {@link AccountService}.
	 * 
	 * @param token the token to identify the user
	 */
	public static void updatePersistenceProviderConfiguration(String token) {
		Users u = ServicePersistenceProvider.getInstance().loadUser(token);
		
		if (u == null) {
			LOGGER.info("Invalid token '{}'!", token);
			return;
		}

		Account account = ServicePersistenceProvider.getInstance().loadAccount(u.getAccountID());
		
		String dbPassword  = account.getDbPassword();
		String host 	   = account.getDbHost();
		String port 	   = Integer.toString(account.getDbPort());
		String name 	   = account.getDbName();
		
		// now set the configuration properties for the user
		if (dbPassword.isEmpty()) {
			PersistenceConfiguration.getSessionSingleton(token).setUsePassword(false);
		} else {
			PersistenceConfiguration.getSessionSingleton(token).setUsePassword(true);
			PersistenceConfiguration.getSessionSingleton(token).updateDBPassword(dbPassword);
		}
		
		PersistenceConfiguration.getSessionSingleton(token).updateDBHost(host);
		PersistenceConfiguration.getSessionSingleton(token).updateDBPort(port);
		PersistenceConfiguration.getSessionSingleton(token).updateDBName(name);
	}
	
}
