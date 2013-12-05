package org.sopeco.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.config.Configuration;
import org.sopeco.persistence.config.PersistenceConfiguration;
import org.sopeco.service.configuration.HTTPStatus;
import org.sopeco.service.persistence.entities.account.Account;
import org.sopeco.service.security.Crypto;
import org.sopeco.service.shared.Message;
import org.sopeco.service.persistence.ServicePersistence;

@Path("account")
public class AccountService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	
	@GET
	@Path("create/{username}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAccount(@PathParam("username") String username,
								  @PathParam("password") String password) {
		
		PersistenceConfiguration c = PersistenceConfiguration.getSessionSingleton(Configuration.getGlobalSessionId());
		boolean status = createAccount(username, password, c.getMetaDataHost(), Integer.parseInt(c.getMetaDataPort()));
		
		if (status) {
			return Response.status(HTTPStatus.Created).build();
		}
		
		return Response.status(HTTPStatus.Forbidden).entity(new Message("Account with the name " + username + " already exists")).build();
	}
	
	private boolean createAccount(String accountName, String password, String dbHost, int dbPort) {
		if (accountExist(accountName)) {
			LOGGER.info("It already exists an account named '{}'", accountName);
			return false;
		}

		Account account = new Account();
		account.setName(accountName);
		account.setPaswordHash(Crypto.sha256(password));

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
	
}
