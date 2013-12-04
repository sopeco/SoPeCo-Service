package org.sopeco.service.shared;

/**
 * The LoginData is given from the Service layer back to the Client
 * when first time requesting the service.
 * 
 * @author Peter Merkert
 */
public class LoginData {

	private String accessToken;
	
	public LoginData(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	
	
}
