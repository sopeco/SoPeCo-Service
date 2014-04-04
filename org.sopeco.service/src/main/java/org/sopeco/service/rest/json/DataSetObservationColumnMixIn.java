package org.sopeco.service.rest.json;

import java.util.List;

import org.sopeco.persistence.dataset.DataSetInputColumn;
import org.sopeco.persistence.dataset.DataSetObservationColumn;
import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.ParameterValueList;
import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MixIn for {@link DataSetObservationColumn} class, which has no default constructor and some
 * getXYZ methods, where XYZ is no field.
 * 
 * @author Peter Merkert
 */
public final class DataSetObservationColumnMixIn<T> {

	@JsonProperty("valueLists")
	private List<ParameterValueList<T>> valueLists;
	
	@JsonCreator
	protected DataSetObservationColumnMixIn(@JsonProperty("parameter") ParameterDefinition parameter, @JsonProperty("valueLists") List<T> values) {
	}

	@JsonIgnore
	public int size() {
		return 0;
	}
	
	@JsonIgnore
	public List<T> getAllValues() {
		return null;
	}

	@JsonIgnore
	private List<ParameterValue<?>> getAllValuesAsParameterValues() {
		return null;
	}
	
	@JsonIgnore
	public double getMin() {
		return 0.0f;
	}

	@JsonIgnore
	public double getMax() {
		return 0.0f;
	}
	
	@JsonIgnore
	public DataSetInputColumn<T> getCopy() {
		return null;
	}
	
	@JsonIgnore
	public List<ParameterValue<?>> getParameterValues() {
		return null;
	}
}
