package org.sopeco.service.shared;

/**
 * The message is used to response with a message to the service
 * requester.
 * This message is intended to use when a request could not be fulfilled.
 * 
 * @author Peter Merkert
 */
public class Message {

	/**
	 * The status is used to transmit the status code.
	 * Primarily it's used to transmit boolean status.
	 */
	private int status;
	
	private String message;
	
	/**
	 * Creates a message.
	 * 
	 * @param message the message
	 */
	public Message() {
		this.message = "";
		this.status = -1;
	}
	
	/**
	 * Creates a message.
	 * 
	 * @param message the message
	 */
	public Message(String message) {
		this.message = message;
	}
	
	/**
	 * Creates a message.
	 * 
	 * @param message the message
	 */
	public Message(String message, int status) {
		this.message = message;
		this.status = status;
	}
	
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return if the status is 0
	 */
	public Boolean failed() {
		return status == 0;
	}
	
	/**
	 * @return if the status is not 0
	 */
	public Boolean ok() {
		return status == 1;
	}
	
}
