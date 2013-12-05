package org.sopeco.service.configuration;

/**
 * The HTTP response status codes are stored in this file to access them globally and document
 * their meaning.
 * Please see {@link http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html} for HTTP/1.1
 * status codes explainations for more information.
 * 
 * @author Peter Merkert
 */
public final class HTTPStatus {

	/**
	 * The request has succeeded.
	 */
	public static final int OK = 200;
	
	/**
	 * The request has been fulfilled and resulted in a new resource being created.
	 */
	public static final int Created = 201;
	
	/**
	 * The request has been accepted for processing, but the processing has not been completed.
	 * The request might or might not eventually be acted upon, as it might be disallowed when
	 * processing actually takes place. There is no facility for re-sending a status code from
	 * an asynchronous operation such as this.
	 */
	public static final int Accepted = 202;
	
	/**
	 * The request could not be understood by the server due to malformed syntax.
	 * The client SHOULD NOT repeat the request without modifications.
	 */
	public static final int BadRequest = 400;
	
	/**
	 * The request requires user authentication.
	 */
	public static final int Unauthorized = 401;
	
	/**
	 * The server understood the request, but is refusing to fulfill it.
	 * Authorization will not help and the request SHOULD NOT be repeated.
	 */
	public static final int Forbidden = 403;

	/**
	 * The server is refusing to service the request because the entity of the request
	 * is in a format not supported by the requested resource for the requested method.
	 */
	public static final int UnsupportedMediaType = 415;
}
