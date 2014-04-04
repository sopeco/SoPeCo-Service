package org.sopeco.service.rest.json;

import org.sopeco.persistence.IPersistenceProvider;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.entities.ExperimentSeries;
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
 * A MixIn for {@link ExperimentSeriesRun}. One property could not be detected
 * by Jackson and some methods caused problems when (de)serializing.<br />
 * In addition, there was a cyclic reference {@link ExperimentSeriesRun}-> {@link ExperimentSeries}
 * -> {@link ExperimentSeriesRun}. This forced a cyclic reference information {@link JsonIdentityInfo}
 * to avoid the endless recursiv adding.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=ExperimentSeriesRun.class, name="experimentSeriesRun"),
	@JsonSubTypes.Type(value=ExperimentSeriesRunDecorator.class, name="experimentSeriesRunDecorator")
}) 
public final class ExperimentSeriesRunMixIn<T> {
	
	@JsonProperty("successfulResultDataSetId")
	private String successfulResultDataSetId;
	
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
