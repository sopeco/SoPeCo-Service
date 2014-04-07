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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * This interface is only used to inject the class {@link org.sopeco.persistence.entities.definition.ParameterNamespace}
 * with a Jackson annotation. The injection is done via Jackson MixIns.
 * <br /><br />
 * The interface annotation with {@code @JsonIdentityInfo} is used to enable a cyclic
 * reference in this class in serializing+deserializing. Jackson normally does not recognize
 * cyclic references as it does not check for the same reference. With a special ID field,
 * Jackson is able to set references to unique IDs. With these IDs Jackson checks for cyclic
 * references.
 * <br /><br />
 * The annotation {@code @JsonIgnore} at the method {@code getFullName()} is used to tell Jackson
 * that this method is no getter for a class/interface field. Otherwise Jackson searches for
 * a field named {@code fullName}, which is not available.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public interface ParameterNamespaceMixIn {

	/**
	 * The annotation {@code @JsonIgnore} at the method {@code getFullName()} is used to tell Jackson
	 * that this method is no getter for a class/interface field. Otherwise Jackson searches for
	 * a field named {@code fullName}, which is not available.
	 * 
	 * @return 	the full namespace string
	 */
	@JsonIgnore
	String getFullName();
	
}
