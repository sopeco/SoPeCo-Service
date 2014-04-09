/**
 * Copyright (c) 2014 SAP
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
package org.sopeco.service.rest.exchange;

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
