package org.sopeco.service.rest;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
	 * @param dbport		the database password
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
	 * Stores the given {@link AccountDetails} in the database. Existing information
	 * will be overwritten. This method is privileged and need a correct token, to modify
	 * the database.
	 * 
	 * @param usertoken			the user authentification
	 * @param accountDetails	the {@link AccountDetails}
	 * @return 					true, if the {@link AccountDetails} could be stored
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> setInfo(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken,
										 	AccountDetails accountDetails) {
		
		if (accountDetails == null) {
			LOGGER.debug("AccountDetails invalid");
			return new ServiceResponse<Boolean>(Status.CONFLICT, false,"AccountDetails invalid");
		}
		
		ServiceResponse<Account> sr_account = getAccount(usertoken);
		
		if (sr_account.getStatus() != Status.OK) {
			LOGGER.debug("Invalid token");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		Account account = sr_account.getObject();
		
		if (account == null) {
			LOGGER.debug("Invalid token");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
	
		// now check if account details correspond to the given token
		if (accountDetails.getId() == account.getId() &&
			accountDetails.getAccountName().equals(account.getName())) {

			ServicePersistenceProvider.getInstance().storeAccountDetails(accountDetails);
			return new ServiceResponse<Boolean>(Status.OK, true);
			
		} else {
			
			LOGGER.debug("Token does not authorize to modify this account.");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
			
		}
	}
	
	/**
	 * Access the {@link AccountDetails} information for a given username.
	 * 
	 * @param accountname 	the accountname the information is requested to
	 * @return 				{@link AccountDetails} object with all the account details
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<AccountDetails> getInfo(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
				
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<AccountDetails>(Status.UNAUTHORIZED, null);
		}	
		
		return new ServiceResponse<AccountDetails>(Status.OK, u.getAccountDetails());
	}
	
	
	/**
	 * Access the account as such with the given user token. The result should not be <code>null</code>,
	 * but might be.
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
			return new ServiceResponse<Account>(Status.UNAUTHORIZED, null);
		}
		
		Account a = ServicePersistenceProvider.getInstance().loadAccount(u.getCurrentAccount().getId());
		
		return new ServiceResponse<Account>(Status.OK, a);
	}
	
	
	/**
	 * Access the account as such with the given user token.
	 * 
	 * @param usertoken the user identification
	 * @return 			the account the current user is related to
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CHECK + "/" + ServiceConfiguration.SVC_ACCOUNT_PASSWORD)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> checkPassword(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
			      								  @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {

		Account account = ServicePersistenceProvider.getInstance().loadAccount(accountname);
	
		if (account == null) {
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		if (!account.getPasswordHash().equals(Crypto.sha256(password))) {
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
	
		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * Access the account as such with the given user token. It's a GET method, but the last
	 * request time for the user is refreshed. Actually a GET should not change anything at
	 * the system. Otherwise this method might say, the token is valid, but in the next millisecond
	 * the tokens get invalid. This is confusing for clients.
	 * 
	 * @param usertoken the user identification
	 * @return 			true, if the given token is valid
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CHECK + "/" + ServiceConfiguration.SVC_ACCOUNT_TOKEN)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> checkToken(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}
		
		// reset the timer for the token
		u.setLastRequestTime(System.currentTimeMillis());
		ServicePersistenceProvider.getInstance().storeUser(u);
		
		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	/**
	 * The login method to authentificate that the current client has the permission to
	 * change something on this account.
	 * This method does only throw an <code>Status.UNAUTHORIZED</code> when the request fails.
	 * This is for security, that attacker cannot guess usernames and get a wrong-right answer
	 * on usernames.
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
			sr.setStatus(Status.UNAUTHORIZED);
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
	
	/**
	 * Logout just means to remove the user with the given token in the database.
	 * 
	 * @param usertoken 	the user authentification
	 * @return 				true, if the logout was successful; false, if the token
	 * 						is not valid
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_ACCOUNT_LOGOUT)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse<Boolean> logout(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		Users u = AccountService.loadUserAndUpdateExpiration(usertoken);
		
		if (u == null) {
			LOGGER.debug("Invalid token.");
			return new ServiceResponse<Boolean>(Status.UNAUTHORIZED, false);
		}

		ServicePersistenceProvider.getInstance().removeUser(u);
		
		return new ServiceResponse<Boolean>(Status.OK, true);
	}
	
	
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// HELPER //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// GLOBAL STATIC HELPER ///////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Loads the {@link Users} with the given token. First it's checked if the token
	 * is still valid. When the token is expired the user (<b>NOT</b> the
	 * account!) is removed from the database.<br />
	 * With this test, the {@link Users} last request time is updated with the current
	 * system time.
	 * 
	 * @param usertoken the unique user token
	 * @return			the {@link Users} with the token, <code>null</code> if the token is
	 * 					invalid or expired
	 */
	public static Users loadUserAndUpdateExpiration(String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.info("Token invalid.");
			return null;
		}
		
		if (u.isExpired()) {
			
			LOGGER.info("Token expired. Login again please.");
			ServicePersistenceProvider.getInstance().removeUser(u);
			return null;
			
		}
		
		u.setLastRequestTime(System.currentTimeMillis());
		
		ServicePersistenceProvider.getInstance().storeUser(u);

		return u;
	}
}
