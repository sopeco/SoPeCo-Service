package org.sopeco.service.shared;

/**
 * The message is used to response with a message to the service
 * requester.
 * This message is intended to use when a request could not be fulfilled.
 * 
 * @author Peter Merkert
 */
public class Message {

	private String message;

	/**
	 * Creates a message.
	 * 
	 * @param message the message
	 */
	public Message(String message) {
		this.message = message;
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
	
}
