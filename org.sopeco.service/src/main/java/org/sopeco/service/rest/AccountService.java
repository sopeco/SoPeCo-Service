package org.sopeco.service.rest;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import org.sopeco.service.security.Crypto;
import org.sopeco.service.shared.Message;
import org.sopeco.service.user.UserManager;
import org.sopeco.service.persistence.ServicePersistence;
import org.sopeco.service.persistence.FlexiblePersistenceProviderFactory;

@Path(ServiceConfiguration.SVC_ACCOUNT)
public class AccountService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	
	/**
	 * Creates a new user Account with the given username and password.
	 * This is a POST request.
	 * 
	 * @param accountname the accountname
	 * @param password the password for the account
	 * @return a {@link Message} with a status and a message string
	 */
	@POST
	@Path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public Message createAccount(@QueryParam("accountname") String accountname,
								 @QueryParam("password") String password) {
		
		PersistenceConfiguration c = PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());
		Message m = createAccount(accountname, password, c.getMetaDataHost(), Integer.parseInt(c.getMetaDataPort()));
		
		return m;
	}
	
	
	
	/**
	 * Checks if an account with the given name exists.
	 * 
	 * @param accountname the accountname
	 * @return true, if the account exists
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_EXISTS)
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean checkExistence(@QueryParam("accountname") String accountname) {
		LOGGER.debug("Trying to check account existence");
		Boolean exists = accountExist(accountname);
		
		return exists;
	}
	
	
	/**
	 * Access the account information for a given username.
	 * 
	 * @param accountname the accountname the information is requested to
	 * @return AccountDetails object with all the account details
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO + "/{accountname}")
	@Produces(MediaType.APPLICATION_JSON)
	public AccountDetails getInfo(@PathParam("accountname") String accountname) {
		
		Long accountID = ServicePersistence.getServicePersistenceProvider().loadAccount(accountname).getId();
		AccountDetails accountDetails = ServicePersistence.getServicePersistenceProvider().loadAccountDetails(accountID);

		return accountDetails;
	}
	
	
	/**
	 * The login method to authentificate that the current client has the permission to
	 * change something on this account.
	 * 
	 * @param accountname the account name to connect to
	 * @param password the password for the account
	 * @return a message, whose status is the token to authentificate afterwards, when it has not failed
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Message loginWithPassword(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
									 @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {
		
		Message m = new Message("", 0);
		
		Account account = ServicePersistence.getServicePersistenceProvider().loadAccount(accountname);

		if (account == null) {
			LOGGER.debug("Account '{}' doesn't exist.", accountname);
			m.setMessage("Account does not exist.");
			return m;
		}
		if (!account.getPasswordHash().equals(Crypto.sha256(password))) {
			LOGGER.debug("Wrong password. Password hashes are not equal!");
			m.setMessage("Wrong password. Password hashes are not equal!");
			return m;
		}

		IPersistenceProvider persistence = null;
		try {
			String databasePassword = Crypto.decrypt(password, account.getDbPassword());
			
			if (databasePassword.isEmpty()) {
				persistence = FlexiblePersistenceProviderFactory.createPersistenceProvider(account.getDbHost(),
																						   account.getDbPort() + "",
																					   	   account.getDbName());
			} else {
				persistence = FlexiblePersistenceProviderFactory.createPersistenceProvider(account.getDbHost(), 
																						   account.getDbPort() + "",
																						   account.getDbName(),
																						   databasePassword);
			}
		} catch (WrongCredentialsException e) {
			LOGGER.warn("Wrong password database credentials!");
			m.setMessage("Wrong password database credentials!");
			return m;
		}

		if (persistence == null) {
			LOGGER.warn("Connection to the database failed.");
			m.setMessage("Connection to the database failed.");
			return m;
		}
		
		// create a unique token for the requester
		String uuid = UUID.randomUUID().toString();

		// login successful, send unique token to user
		m.setStatus(1);
		m.setMessage(uuid);
		
		// set the user persistence provider
		UserManager.instance().registerUser(uuid);
		UserManager.instance().getUser(uuid).setCurrentPersistenceProvider(persistence);
		
		AccountDetails details = ServicePersistence.getServicePersistenceProvider().loadAccountDetails(account.getId());
		if (details == null) {
			details = new AccountDetails();
			details.setId(account.getId());
			details.setAccountName(account.getName());
			ServicePersistence.getServicePersistenceProvider().storeAccountDetails(details);
		}

		return m;
	}
	
	
	
	/*****************HELPER***********************/
	
	/**
	 * Creates an fresh new account with all the given settings. The account is stored in the database.
	 * 
	 * @param accountName the name for this account
	 * @param password the password to login into this account
	 * @param dbHost the database for this account
	 * @param dbPort the database port for this account
	 * @return message with the status, which indicates if the account could be created
	 */
	private Message createAccount(String accountName, String password, String dbHost, int dbPort) {
		
		if (accountExist(accountName)) {
			LOGGER.info("It already exists an account named '{}'", accountName);
			
			return new Message("Account with the name \"" + accountName + "\" already exists", 0);
		}

		Account account = new Account();
		account.setName(accountName);
		account.setPasswordHash(Crypto.sha256(password));
		account.setDbHost(dbHost);
		account.setDbPort(dbPort);
		account.setDbName(ServiceConfiguration.SVC_DB_PREFIX + " " + accountName);
		account.setDbPassword(Crypto.encrypt(password, password));
		account.setLastInteraction(-1);

		account = ServicePersistence.getServicePersistenceProvider().storeAccount(account);

		LOGGER.debug("Account created with id {}", account.getId());

		return new Message("Account successfully created", 1);
	}

	private boolean accountExist(String accountName) {
		Account testIfExist = ServicePersistence.getServicePersistenceProvider().loadAccount(accountName);

		if (testIfExist == null) {
			return false;
		} else {
			return true;
		}
	}
	
}
