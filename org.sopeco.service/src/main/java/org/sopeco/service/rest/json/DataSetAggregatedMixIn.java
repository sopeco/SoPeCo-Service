package org.sopeco.service.rest.json;

import java.util.Collection;
import java.util.List;

import org.sopeco.persistence.dataset.AbstractDataSetColumn;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.DataSetRow;
import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.ParameterValueList;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.persistence.util.ParameterCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MixIn for {@link DataSetAggregated} class, which has some getXYZ methods,
 * where XYZ is no field.
 * 
 * @author Peter Merkert
 */
public final class DataSetAggregatedMixIn<T> {

	@JsonProperty("size")
	private int size;
	
	@JsonProperty("id")
	private String id;
	
	@JsonIgnore
	public List<DataSetRow> getRowList() {
		return null;
	}

	@JsonIgnore
	DataSetAggregated getSubSet(ParameterDefinition xParameter, ParameterDefinition yParameter) {
		return null;
	}

	@JsonIgnore
	public DataSetAggregated getSubSet(Collection<ParameterDefinition> parameterList) {
		return null;
	}

	@JsonIgnore
	public List<ParameterDefinition> getVariedInputParameters() {
		return null;
	}

	@JsonIgnore
	public DataSetAggregated getVariedSubSet() {
		return null;
	}

	@JsonIgnore
	public Collection<ParameterValueList<?>> getObservationParameterValues(ParameterCollection<ParameterValue<?>> inputParameterValues) throws DataNotFoundException {
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	@JsonIgnore
	public Collection<AbstractDataSetColumn> getColumns() {
		return null;
	}
	
	@JsonIgnore
	public Collection<ParameterDefinition> getParameterDefinitions() {
		return null;
	}

}
