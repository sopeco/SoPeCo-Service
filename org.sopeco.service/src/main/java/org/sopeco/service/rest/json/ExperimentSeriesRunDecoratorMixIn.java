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

import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.exceptions.ExperimentFailedException;
import org.sopeco.service.rest.exchange.ExperimentSeriesRunDecorator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A MixIn for {@link ExperimentSeriesRunDecorator}. A lot of fields could not be
 * detected by Jackson, because they are private and have no public setter or any getter.
 * Some method caused problems when (de)serializing, too.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=ExperimentSeriesRun.class, name="experimentSeriesRun"),
	@JsonSubTypes.Type(value=ExperimentSeriesRunDecorator.class, name="experimentSeriesRunDecorator")
}) 
public final class ExperimentSeriesRunDecoratorMixIn {
	
	@JsonProperty("accountID")
	private long accountID;

	@JsonProperty("serviceHTTPprefix")
	private String serviceHTTPprefix;
	
	@JsonProperty("host")
	private String host;
	
	@JsonProperty("port")
	private String port;

	@JsonProperty("urlSplitSign")
	private String urlSplitSign;

	@JsonProperty("hostPortSplitSign")
	private String hostPortSplitSign;
	
	@JsonIgnore
	public Long getPrimaryKey() {
		return 0L;
	}

	@JsonIgnore
	public DataSetAggregated getSuccessfulResultDataSet() {
		return null;
	}
	
	@JsonIgnore
	public void setSuccessfulResultDataSet(DataSetAggregated resultDataSet) {
	}
	
	@JsonIgnore
	public void addExperimentFailedException(ExperimentFailedException exception) {
	}

	@JsonIgnore
	public void storeDataSets(IPersistenceProvider provider) {
	}

	@JsonIgnore
	public void removeDataSets(IPersistenceProvider provider) {
	}

	@JsonIgnore
	public void appendSuccessfulResults(DataSetAggregated experimentRunResults) {
	}

	@JsonIgnore
	public IPersistenceProvider getPersistenceProvider() {
		return null;
	}
	
	@JsonIgnore
	public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
	}
	
	@JsonIgnore
	public String getDatasetId(){
		return null;
	}
}
