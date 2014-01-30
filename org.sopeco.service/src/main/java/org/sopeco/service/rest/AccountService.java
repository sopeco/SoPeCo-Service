package org.sopeco.service.rest;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.security.Crypto;
import org.sopeco.service.shared.Message;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;

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
	public Message createAccount(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
								 @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {
		
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
	public Boolean checkExistence(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname) {
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
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO)
	@Produces(MediaType.APPLICATION_JSON)
	public AccountDetails getInfo(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname) {
		
		Long accountID = ServicePersistenceProvider.getInstance().loadAccount(accountname).getId();
		AccountDetails accountDetails = ServicePersistenceProvider.getInstance().loadAccountDetails(accountID);

		return accountDetails;
	}
	

	/**
	 * Access the account as such with the given user token.
	 * 
	 * @param usertoken the user identification
	 * @return the account the current user is related to
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
	@Produces(MediaType.APPLICATION_JSON)
	public Account getAccount(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		return ServicePersistenceProvider.getInstance().loadAccount(u.getCurrentAccount().getId());
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
		
		Account account = ServicePersistenceProvider.getInstance().loadAccount(accountname);

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
		
		// create a unique token for the requester
		String uuid = UUID.randomUUID().toString();

		// login successful, send unique token to user
		m.setStatus(1);
		m.setMessage(uuid);
		
		// save the current user
		Users u = new Users(uuid);
		u.setCurrentAccount(account);
		ServicePersistenceProvider.getInstance().storeUser(u);

		// update the account details for a user
		AccountDetails details = ServicePersistenceProvider.getInstance().loadAccountDetails(account.getId());
		if (details == null) {
			details = new AccountDetails();
			details.setId(account.getId());
			details.setAccountName(account.getName());
			ServicePersistenceProvider.getInstance().storeAccountDetails(details);
		}
		
		// update the SoPeCo configuration for the configuration with the usertoken
		UserPersistenceProvider.updatePersistenceProviderConfiguration(uuid);

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
		account.setDbName(ServiceConfiguration.SVC_DB_PREFIX + "_" + accountName);
		account.setDbPassword(password);
		account.setLastInteraction(-1);

		account = ServicePersistenceProvider.getInstance().storeAccount(account);

		LOGGER.debug("Account created with id {}", account.getId());

		return new Message("Account successfully created", 1);
	}

	private boolean accountExist(String accountName) {
		Account testIfExist = ServicePersistenceProvider.getInstance().loadAccount(accountName);

		if (testIfExist == null) {
			return false;
		} else {
			return true;
		}
	}
	
}
