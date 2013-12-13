/**
 * Copyright (c) 2013 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores all the user the service currently is managing in a map, where the
 * unique UUID is mapped to a definied user.
 * 
 * @author Peter Merkert
 */
public final class UserManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

	private static UserManager singleton;
	private Map<String, User> userMap = new HashMap<String, User>();

	/**
	 * Private constructor for singleton.
	 */
	private UserManager() {
	}
	
	/**
	 * Singleton instance request.
	 * 
	 * @return the usermanager singleton
	 */
	public static UserManager instance() {
		if (singleton == null) {
			singleton = new UserManager();
		}
		return singleton;
	}


	/**
	 * Returns whether a (not expired) user with the given session id exists in
	 * the userMap.
	 * 
	 * @param token the unique token (UUID String)
	 * @return user with the given session exists
	 */
	public boolean existUser(String token) {
		synchronized (userMap) {
			return getUser(token) != null;
		}
	}

	/**
	 * Returns a List with all users which are connected to the given account.
	 * 
	 * @param accountId the account id
	 * @return list with all users connected to account with given accountid.
	 */
	public List<User> getAllUserOnAccount(long accountId) {
		List<User> userList = new ArrayList<User>();
		
		for (User u : userMap.values()) {
			if (u.isExpired()) {
				destroyUser(u);
			} else if (u.getCurrentAccount().getId() == accountId) {
				userList.add(u);
			}
		}
		
		return userList;
	}

	/**
	 * Returns a list with all users, which are not expired.
	 * 
	 * @return a list with all not expired users.
	 */
	public List<User> getAllUsers() {
		List<User> userList = new ArrayList<User>();
		
		for (User u : userMap.values()) {
			if (u.isExpired()) {
				destroyUser(u);
			} else {
				userList.add(u);
			}
		}
		
		return userList;
	}
	
	/**
	 * Returns the user, which has the given session id. If there is no user
	 * with the given session key, it returns null.
	 * 
	 * @param token the unique token for the user
	 * @return user correspondign to given token
	 */
	public User getUser(String token) {
		
		synchronized (userMap) {
			
			User user = userMap.get(token);
			if (user != null && user.isExpired()) {
				destroyUser(user);
				user = null;
			}
			
			return user;
		}
		
	}

	/**
	 * Registers a new user with the given token.
	 * 
	 * @param token the unique token, which the users knows, too
	 */
	public void registerUser(String token) {
		LOGGER.debug("Store new user with the token: '{}'", token);
		User newUser = new User(token);
		userMap.put(token, newUser);
	}

	/**
	 * Destroys the given user.
	 * 
	 * @param user
	 *            to destroy
	 */
	public void destroyUser(User u) {
		LOGGER.debug("Destroy user with token: '{}'", u.getToken());

		userMap.remove(u.getToken());

		// remove the persistence provider connected to the user u
		if (u.getCurrentPersistenceProvider() != null) {
			u.getCurrentPersistenceProvider().closeProvider();
			u.setCurrentPersistenceProvider(null);
		}
	}
}
