package org.sopeco.service.rest;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.Users;
import org.sopeco.service.rest.exchange.ServiceResponse;
import org.sopeco.service.security.Crypto;
import org.sopeco.service.persistence.ServicePersistenceProvider;
import org.sopeco.service.persistence.UserPersistenceProvider;

/**
 * Add the RESTful service for account handling. This service class enables e.g. account creation.
 * However, the login function might be the most frequently used one.
 * 
 * @author Peter Merkert
 */
@Path(ServiceConfiguration.SVC_ACCOUNT)
public class AccountService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	
	/**
	 * Creates a new user Account with the given username and password.
	 * Uses the configuration default values for database choice.
	 * 
	 * @param accountname 	the accountname
	 * @param password	 	the password for the account
	 * @return 				true, if the account creation was succesful
	 */
	@POST
	@Path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> createAccount(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
								 			      @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {
		
		PersistenceConfiguration c = PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());

		return createAccountCustomized(accountname, password, c.getMetaDataHost(), c.getMetaDataPort());
	}
	
	/**
	 * Creates a new user Account with the given username and password and customized database
	 * credentials.
	 * 
	 * @param accountname 	the accountname
	 * @param password	 	the password for the account
	 * @param dbname		the database name
	 * @param dbport	the database password
	 * @return 				true, if the account creation was succesful
	 */
	@POST
	@Path(ServiceConfiguration.SVC_ACCOUNT_CREATE + "/" + ServiceConfiguration.SVC_ACCOUNT_CUSTOMIZE)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> createAccountCustomized(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
							 			      			 	@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password,
							 			      			 	@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_DATABASENAME) String dbname,
							 			      			 	@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_DATABASEPORT) String dbport) {

		ServiceResponse<Boolean> sr = createAccount(accountname, password, dbname, Integer.parseInt(dbport));
		
		return sr;
	}
	
	/**
	 * Checks if an account with the given name exists.
	 * 
	 * @param accountname 	the accountname
	 * @return 				true, if the account exists
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_EXISTS)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> checkExistence(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname) {
		LOGGER.debug("Trying to check account existence");
		Boolean exists = accountExist(accountname);
		
		return new ServiceResponse<Boolean>(Status.OK, exists);
	}
	
	
	/**
	 * Access the account information for a given username.
	 * 
	 * @param accountname 	the accountname the information is requested to
	 * @return 				{@link AccountDetails} object with all the account details
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<AccountDetails> getInfo(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname) {
		
		Long accountID = ServicePersistenceProvider.getInstance().loadAccount(accountname).getId();
		AccountDetails accountDetails = ServicePersistenceProvider.getInstance().loadAccountDetails(accountID);

		return new ServiceResponse<AccountDetails>(Status.OK, accountDetails);
	}
	

	/**
	 * Access the account as such with the given user token.
	 * 
	 * @param usertoken the user identification
	 * @return 			the account the current user is related to
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Account> getAccount(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return null;
		}
		
		Account a = ServicePersistenceProvider.getInstance().loadAccount(u.getCurrentAccount().getId());
		
		return new ServiceResponse<Account>(Status.OK, a);
	}
	
	
	/**
	 * The login method to authentificate that the current client has the permission to
	 * change something on this account.
	 * 
	 * @param accountname 	the account name to connect to
	 * @param password 		the password for the account
	 * @return 				a message, whose status is the token to authentificate afterwards, when it has not failed
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<String> loginWithPassword(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
									 				 @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {
		
		ServiceResponse<String> sr = new ServiceResponse<String>();
		
		Account account = ServicePersistenceProvider.getInstance().loadAccount(accountname);

		if (account == null) {
			LOGGER.debug("Account '{}' doesn't exist.", accountname);
			sr.setMessage("Account does not exist.");
			sr.setStatus(Status.FORBIDDEN);
			return sr;
		}
		if (!account.getPasswordHash().equals(Crypto.sha256(password))) {
			LOGGER.debug("Wrong password. Password hashes are not equal!");
			sr.setMessage("Wrong password. Password hashes are not equal!");
			sr.setStatus(Status.UNAUTHORIZED);
			return sr;
		}
		
		// create a unique token for the requester
		String uuid = UUID.randomUUID().toString();

		// login successful, send unique token to user
		sr.setMessage(uuid);
		sr.setObject(uuid);
		sr.setStatus(Status.OK);
		
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

		return sr;
	}
	
	
	
	/*****************HELPER***********************/
	
	/**
	 * Creates an fresh new account with all the given settings. The account is stored in the database.
	 * 
	 * @param accountName 	the name for this account
	 * @param password  	the password to login into this account
	 * @param dbHost 		the database for this account
	 * @param dbPort 		the database port for this account
	 * @return            	message with the status, which indicates if the account could be created
	 */
	private ServiceResponse<Boolean> createAccount(String accountName, String password, String dbHost, int dbPort) {
		
		if (accountExist(accountName)) {
			LOGGER.info("It already exists an account named '{}'", accountName);
			
			return new ServiceResponse<Boolean>(Status.FORBIDDEN, false, "Account with the name \"" + accountName + "\" already exists.");
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

		return new ServiceResponse<Boolean>(Status.OK, true, "Account with the name \"" + accountName + "\" created!");
	}

	/**
	 * Checks the database for the given account name.
	 * 
	 * @param accountName the name to check
	 * @return            true, if the name does not exist
	 */
	private boolean accountExist(String accountName) {
		Account testIfExist = ServicePersistenceProvider.getInstance().loadAccount(accountName);

		return testIfExist != null;
	}
	
}
