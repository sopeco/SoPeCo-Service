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
package org.sopeco.service.rest.json;

import org.sopeco.config.IConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class' only task is to provide a JSON annotation for a set method.
 * The method setProperties(IConfiguration configuration) was the default
 * setter method in the class {@link ScheduledExperiment}. Jackson tried to
 * use the setter with instantiating an object of interface type
 * {@link IConfiguration} which led to a Java exception.<br />
 * To tell Jackson, to ignore this method the annotation is used. As it is not
 * possible to manipulate the original <code>ScheduledExperiment</code> class
 * this MixIn is used.
 * 
 * @author Peter Merkert
 */
public final class ScheduledExperimentMixIn {

	/**
	 * The setProperties method must be ignore by Json, because otherwise it
	 * tries to call the method with an object of type {@link IConfiguration}.
	 * This is going to fail, because <code>IConfiguration</code> is an
	 * interface and cannot be instantiated.
	 * 
	 * @param configuration the configuration
	 */
	@JsonIgnore
	public void setProperties(IConfiguration configuration) {
	}
	
	/**
	 * The method is used for a better acces to the experimentkey. The original
	 * key is calculated via the hash code of the {@link ScheduledExperiment} and
	 * therefor is no class field. But Jackson tries to may this getter to a 
	 * property of the class and fails.<br />
	 * A <code>@JsonIgnore</code> is needed.
	 * 
	 * @return the experiment key
	 */
	@JsonIgnore
	public int getExperimentKey() {
		return 0;
	}
	
}
