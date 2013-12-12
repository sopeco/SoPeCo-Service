package org.sopeco.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.persistence.exceptions.WrongCredentialsException;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.account.Account;
import org.sopeco.service.persistence.entities.account.AccountDetails;
import org.sopeco.service.persistence.entities.account.RememberMeToken;
import org.sopeco.service.security.Crypto;
import org.sopeco.service.shared.LoginData;
import org.sopeco.service.shared.Message;
import org.sopeco.service.persistence.FlexiblePersistenceProviderFactory;
import org.sopeco.service.persistence.ServicePersistence;
import org.sopeco.service.user.UserManager;

@Path("account")
public class AccountService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	
	/**
	 * Creates a new user Account with the given username and password.
	 * 
	 * @param username the username
	 * @param password the 
	 * @return
	 */
	@GET
	@Path("create/{username}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Message createAccount(@PathParam("username") String username,
								  @PathParam("password") String password) {
		
		PersistenceConfiguration c = PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());
		boolean status = createAccount(username, password, c.getMetaDataHost(), Integer.parseInt(c.getMetaDataPort()));
		
		if (status) {
			return new Message("Account successfully created", 1);
		}
		
		return new Message("Account with the name \"" + username + "\" already exists", 0);
	}
	
	private boolean createAccount(String accountName, String password, String dbHost, int dbPort) {
		if (accountExist(accountName)) {
			LOGGER.info("It already exists an account named '{}'", accountName);
			return false;
		}

		Account account = new Account();
		account.setName(accountName);
		account.setPasswordHash(Crypto.sha256(password));

		account.setDbHost(dbHost);
		account.setDbPort(dbPort);
		account.setDbName(accountName);
		account.setDbPassword(Crypto.encrypt(password, password));

		account.setLastInteraction(-1);

		account = ServicePersistence.getServicePersistenceProvider().storeAccount(account);

		LOGGER.debug("Account created with id {}", account.getId());

		return true;
	}

	private boolean accountExist(String accountName) {
		Account testIfExist = ServicePersistence.getServicePersistenceProvider().loadAccount(accountName);

		if (testIfExist == null) {
			return false;
		} else {
			return true;
		}
	}
	
	@GET
	@Path("user/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean checkExistence(@PathParam("username") String username) {
		LOGGER.debug("Trying to check account existence");
		Boolean exists = accountExist(username);
		
		if (exists) {
			return true;
		} else {
			return false;
		}
	}
	
	
	@GET
	@Path("login/{username}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public LoginData login(@PathParam("username") String username,
							@PathParam("password") String password) {
		
		LoginData logindata = new LoginData();
		
		Account account = ServicePersistence.getServicePersistenceProvider().loadAccount(username);

		if (account == null) {
			LOGGER.debug("Account '{}' doesn't exist.", username);
			return logindata;
		}
		
		if (!account.getPasswordHash().equals(Crypto.sha256(password))) {
			LOGGER.debug("Wrong password. Password hashes are not equal!");
			return logindata;
		}

		IPersistenceProvider persistence = null;
		try {
			String databasePassword = Crypto.decrypt(password, account.getDbPassword());
			if (databasePassword.isEmpty()) {
				persistence = FlexiblePersistenceProviderFactory.createPersistenceProvider(account.getDbHost(),
						account.getDbPort() + "", account.getDbName());
			} else {
				persistence = FlexiblePersistenceProviderFactory.createPersistenceProvider(account.getDbHost(),
						account.getDbPort() + "", account.getDbName(), databasePassword);
			}
		} catch (WrongCredentialsException e) {
			LOGGER.warn("Wrong password database credentials!");
			return logindata;
		}

		if (persistence == null) {
			LOGGER.warn("Connection to the database failed.");
			return logindata;
		}

		// Login successfull
		logindata.setStatus(true);

		// Create token to remeber login for later sessions
		String secretToken = Crypto.sha256(System.currentTimeMillis() + username);
		logindata.setAccessToken(secretToken);

		RememberMeToken rememberMeToken = new RememberMeToken();
		rememberMeToken.setTokenHash(Crypto.sha256(secretToken));
		rememberMeToken.setAccountId(account.getId());
		rememberMeToken.setExpireTimestamp(System.currentTimeMillis() + 1000 * 3600 * 24 * 7);
		rememberMeToken.setEncrypted(Crypto.encrypt(secretToken, password));

		ServicePersistence.getServicePersistenceProvider().storeRememberMeToken(rememberMeToken);

		// store loged in user
		UserManager.instance().getAllUsers();
		UserManager.instance().registerUser(ServiceConfiguration.SESSION_ID);
		
		UserManager.instance().getUser(ServiceConfiguration.SESSION_ID).setCurrentAccount(account);
		UserManager.instance().getUser(ServiceConfiguration.SESSION_ID).setCurrentPersistenceProvider(persistence);

		AccountDetails details = ServicePersistence.getServicePersistenceProvider().loadAccountDetails(account.getId());
		if (details == null) {
			details = new AccountDetails();
			details.setId(account.getId());
			details.setAccountName(account.getName());
			ServicePersistence.getServicePersistenceProvider().storeAccountDetails(details);
		}

		return logindata;
	}
	
}
