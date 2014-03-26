package org.sopeco.service.rest;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.ServiceConfiguration;
import org.sopeco.service.helper.Crypto;
import org.sopeco.service.persistence.entities.Account;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.Users;
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
	 * @return 				{@link Response} OK or CONFLICT<br />
	 */
	@POST
	@Path(ServiceConfiguration.SVC_ACCOUNT_CREATE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAccount(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
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
	 * @return 				{@link Response} OK or CONFLICT<br />
	 */
	@POST
	@Path(ServiceConfiguration.SVC_ACCOUNT_CREATE + "/" + ServiceConfiguration.SVC_ACCOUNT_CUSTOMIZE)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAccountCustomized(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
			 			      			 	@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password,
			 			      			 	@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_DATABASENAME) String dbname,
			 			      			 	@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_DATABASEPORT) String dbport) {
		
		return createAccount(accountname, password, dbname, Integer.parseInt(dbport));
	}
	
	/**
	 * Checks if an account with the given name exists.
	 * 
	 * @param accountname 	the accountname
	 * @return 				{@link Response} OK with Boolean: true, if the account exists
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_EXISTS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkExistence(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname) {
		LOGGER.debug("Trying to check account existence");
		Boolean exists = accountExist(accountname);
		return Response.ok(exists).build();
	}
	
	
	/**
	 * Access the {@link AccountDetails} information for a given username.
	 * 
	 * @param usertoken 	the user identification
	 * @return 				{@link Response} with {@link AccountDetails} (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfo(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
				
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}	
		
		AccountDetails ad = ServicePersistenceProvider.getInstance().loadAccountDetails(u.getAccountID());
		
		return Response.ok(ad).build();
	}
	
	/**
	 * Stores the given {@link AccountDetails} in the database. Existing information
	 * will be overwritten. This method is privileged and need a correct token, to modify
	 * the database.
	 * 
	 * @param usertoken			the user authentification
	 * @param accountDetails	the {@link AccountDetails}
	 * @return 					{@link Response} OK, CONFLICT or UNAUTHORIZED
	 */
	@PUT
	@Path(ServiceConfiguration.SVC_ACCOUNT_INFO)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setInfo(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken,
							AccountDetails accountDetails) {
		
		if (accountDetails == null) {
			LOGGER.debug("AccountDetails invalid");
			return Response.status(Status.CONFLICT).entity("AccountDetails invalid").build();
		}
		
		Response r = getAccount(usertoken);
		
		if (r.getStatus() != Status.OK.getStatusCode()) {
			LOGGER.debug("Invalid token");
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		/*
		 *  r.readEntity(Account.class) is not allowed here, as we have not the correct
		 *  ObjectMapper injected. Anf furthermore Jersey throws and error, that this method is not allowed
		 *  when it's called in the same class. r.readEntity() fixes the issue.
		 */
		Account account = (Account) r.getEntity();
		
		if (account == null) {
			LOGGER.debug("Invalid token");
			return Response.status(Status.UNAUTHORIZED).build();
		}
	
		// now check if account details correspond to the given token
		if (accountDetails.getId() == account.getId() &&
			accountDetails.getAccountName().equals(account.getName())) {

			ServicePersistenceProvider.getInstance().storeAccountDetails(accountDetails);
			return Response.ok().build();
			
		} else {
			
			LOGGER.debug("Token does not authorize to modify this account.");
			return Response.status(Status.UNAUTHORIZED).build();
			
		}
	}

	/**
	 * Access the account as such with the given user token.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK or UNAUTHORIZED<br />
	 * 					OK with {@link Account} (null possible)
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CONNECTED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccount(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		Account a = ServicePersistenceProvider.getInstance().loadAccount(u.getAccountID());
		
		return Response.ok(a).build();
	}
	
	/**
	 * Access the account as such with the given user token.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} the account the current user is related to
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CHECK + "/" + ServiceConfiguration.SVC_ACCOUNT_PASSWORD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkPassword(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
			      								  @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {

		Account account = ServicePersistenceProvider.getInstance().loadAccount(accountname);
	
		if (account == null) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (!account.getPasswordHash().equals(Crypto.sha256(password))) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	
		return Response.ok().build();
	}
	
	/**
	 * Access the account as such with the given user token. It's a GET method, but the last
	 * request time for the user is refreshed. Actually a GET should not change anything at
	 * the system. Otherwise this method might say, the token is valid, but in the next millisecond
	 * the tokens get invalid. This is confusing for clients.
	 * 
	 * @param usertoken the user identification
	 * @return 			{@link Response} OK or UNAUTHORIZED
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_CHECK + "/" + ServiceConfiguration.SVC_ACCOUNT_TOKEN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkToken(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {

		Users u = ServicePersistenceProvider.getInstance().loadUser(usertoken);
		
		if (u == null) {
			LOGGER.warn("Invalid token '{}'!", usertoken);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		// reset the timer for the token
		u.setLastRequestTime(System.currentTimeMillis());
		ServicePersistenceProvider.getInstance().storeUser(u);

		return Response.ok().build();
	}
	
	/**
	 * The login method to authentificate that the current client has the permission to
	 * change something on this account.
	 * 
	 * @param accountname 	the account name to connect to
	 * @param password 		the password for the account
	 * @return 				{@link Response} OK or UNAUTHORIZED<br />
	 * 						OK with a the token as String
	 */
	@GET
	@Path(ServiceConfiguration.SVC_ACCOUNT_LOGIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginWithPassword(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_NAME) String accountname,
					 				  @QueryParam(ServiceConfiguration.SVCP_ACCOUNT_PASSWORD) String password) {
		
		Account account = ServicePersistenceProvider.getInstance().loadAccount(accountname);

		if (account == null) {
			LOGGER.debug("Account '{}' doesn't exist.", accountname);
			return Response.status(Status.UNAUTHORIZED).entity("Account does not exist.").build();
		}
		
		if (!account.getPasswordHash().equals(Crypto.sha256(password))) {
			LOGGER.debug("Wrong password. Password hashes are not equal!");
			return Response.status(Status.UNAUTHORIZED).entity("Wrong password. Password hashes are not equal!").build();
		}
		
		// create a unique token for the requester
		String uuid = UUID.randomUUID().toString();
		
		// save the current user
		Users u = new Users(uuid, account.getId());
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

		return Response.ok(uuid).build();
	}
	
	/**
	 * Logouts the user with the given token. The user will be deleted afterwards from
	 * the database.
	 * 
	 * @param usertoken 	the user authentification
	 * @return 				{@link Response} OK or UNAUTHORIZED
	 */
	@DELETE
	@Path(ServiceConfiguration.SVC_ACCOUNT_LOGOUT)
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@QueryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN) String usertoken) {
		
		Users u = AccountService.loadUserAndUpdateExpiration(usertoken);
		
		if (u == null) {
			LOGGER.debug("Invalid token.");
			return Response.status(Status.UNAUTHORIZED).build();
		}

		ServicePersistenceProvider.getInstance().removeUser(u);

		return Response.ok().build();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////// HELPER /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates an fresh new account with all the given settings. The account is stored in the database.
	 * 
	 * @param accountName 	the name for this account
	 * @param password  	the password to login into this account
	 * @param dbHost 		the database for this account
	 * @param dbPort 		the database port for this account
	 * @return            	{@link Response} OK or CONFLICT
	 */
	private Response createAccount(String accountName, String password, String dbHost, int dbPort) {
		
		if (accountExist(accountName)) {
			LOGGER.info("It already exists an account named '{}'", accountName);
			return Response.status(Status.CONFLICT).entity("Account with the name \"" + accountName + "\" already exists.").build();
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

		return Response.ok().build();
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
