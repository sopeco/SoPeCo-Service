package org.sopeco.service.shared;

/**
 * The LoginData is given from the Service layer back to the Client
 * when first time requesting the service.
 * 
 * @author Peter Merkert
 */
public class LoginData {

	private String accessToken;

	private Boolean status;

	public LoginData() {
		this.accessToken = "";
		this.status = false;
	}
	
	public LoginData(String accessToken, Boolean status) {
		this.accessToken = accessToken;
		this.status = status;
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
