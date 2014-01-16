package org.sopeco.service.shared;

/**
 * This object is used to be shared between the RESTful service and the requester.
 * The status must only be in the range [0; 3]. If its outside, something wrong happened
 * before setting the @Code{MECStatus}.
 * 
 * @author Peter Merkert
 */
public class MECStatus {

	private int status;
	
	/**
	 * Controller status is offline.
	 */
	public static int STATUS_OFFLINE = 0;
	
	/**
	 * Checked controller is online.
	 */
	public static int STATUS_ONLINE = 1;
	
	/**
	 * Checked controller is online but can't return any information about the
	 * me.
	 */
	public static int STATUS_ONLINE_NO_META = 2;
	
	/**
	 * The given url is not valid.
	 */
	public static int NO_VALID_MEC_URL = 3;
	
	/**
	 * Default constructor for JSON mapping.
	 * Sets the status to "-1".
	 */
	public MECStatus() {
		this(-1);
	}
	
	/**
	 * Constructor to add immeditely the status.
	 * 
	 * @param status the status of the controller
	 */
	public MECStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
	
}
