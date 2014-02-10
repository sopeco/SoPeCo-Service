package org.sopeco.service.shared;

import javax.ws.rs.core.Response.Status;

/**
 * The {@link ServiceResponse} is used for data exchange at the RESTful interfaces.
 * The class is used to handle HTTP Repsonses and the return object.<br />
 * <br />
 * The main problem was, that Servlets throws a HTTP 204 status when <i>null</i> is
 * returned. This error must be catched by the client. But actually this is a bad style,
 * because the client then needs a tr-ycatch block for every single request.
 * <br />
 * In addition, the HTTP response cannot be queried that easy, when returning a complex
 * object in JSOn format, as the status as such is not provided.
 * 
 * @author Peter Merkert
 *
 * @param <T> The class type which this ServiceResponse is holding.
 */
public class ServiceResponse<T> {
	
	/**
	 * The message for the client.
	 */
	private String message = "";

	/**
	 * The HTTP status is stored in.
	 */
	private Status status = null;
	
	/**
	 * The object which this data exchange object is handling.
	 */
	private T object = null;
	
	/**
	 * Creates a Service response w
	 */
	public ServiceResponse() {
	}
	
	/**
	 * This constructor only take a status. No object is set, instead
	 * <i>null</i> is passed to the next constructor.
	 * 
	 * @param status
	 */
	public ServiceResponse(Status status) {
		this(status, null, "");
	}
	
	/**
	 * A new {@link ServiceResponse} is created with the given object and
	 * status. The message will be left empty.
	 * 
	 * @param object the object to pass
	 * @param status the HTTP status
	 */
	public ServiceResponse(Status status, T object) {
		this(status, object, "");
	}

	/**
	 * A new {@link ServiceResponse} is created with the given object and
	 * status. The message will be left empty.
	 * 
	 * @param object the object to pass
	 * @param status the HTTP status
	 */
	public ServiceResponse(Status status, String message) {
		this(status, null, message);
	}
	
	/**
	 * The general constructor to set all attributes of the {@link ServiceResponse}.
	 * 
	 * @param object  the object to pass
	 * @param status  the HTTP status
	 * @param message the message
	 */
	public ServiceResponse(Status status, T object, String message) {
		this.object  = object;
		this.status  = status;
		this.message = message;
	}
	
	/**
	 * Returns the object.
	 * 
	 * @return the object
	 */
	public T getObject() {
		return object;
	}
	
	/**
	 * Sets the object to the given object.
	 * 
	 * @param object the object to set
	 */
	public void setObject(T object) {
		this.object = object;
	}
	
	/**
	 * Returns the HTTP status of this {@link ServiceResponse}.
	 * 
	 * @return the HTTP status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the HTTP status of this {@link ServiceResponse}.
	 * 
	 * @param status the HTTP status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Returns the message bind to this {@link ServiceResponse}.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message bind to this {@link ServiceResponse}.
	 * 
	 * @param message the message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
}
