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

import java.util.List;

import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.ParameterValueList;
import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Just another MixIn for {@link ParameterValueList}. Some methods caused
 * problems when (de)serializing.
 * 
 * @author Peter Merkert
 */
public final class ParameterValueListMixIn<T> {

	@JsonProperty("parameter")
	private ParameterDefinition parameter;
	
	@JsonProperty("values")
	private List<T> values;
	
	@JsonCreator
	public ParameterValueListMixIn(@JsonProperty("parameter") ParameterDefinition parameter, @JsonProperty("values") List<T> values) {
	}

	@JsonIgnore
	public double getMean() {
		return 0.0f;
	}
	
	@JsonIgnore
	public ParameterValue<?> getMeanAsParameterValue() {
		return null;
	}

	@JsonIgnore
	public List<String> getValueStrings() {
		return null;
	}

	@JsonIgnore
	public List<Double> getValuesAsDouble() {
		return null;
	}

	@JsonIgnore
	public List<Integer> getValuesAsInteger() {
		return null;
	}

	@JsonIgnore
	public int getSize() {
		return 0;
	}

}
