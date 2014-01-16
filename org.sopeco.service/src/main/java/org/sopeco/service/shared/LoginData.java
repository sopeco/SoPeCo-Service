package org.sopeco.service.shared;

/**
 * The LoginData is given from the Service layer back to the Client
 * when first time requesting the service.
 * 
 * @author Peter Merkert
 */
public class LoginData {

	private String token;

	private Boolean status;

	/**
	 * Empty param constructor for JSON mapping.
	 */
	public LoginData() {
		this.token = "";
		this.status = false;
	}
	
	public LoginData(String accessToken, Boolean status) {
		this.token = accessToken;
		this.status = status;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return token;
	}

	/**
	 * @param accessToken the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.token = accessToken;
	}

	/**
	 * @return the status
	 */
	public Boolean getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}
	
}
