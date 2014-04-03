package org.sopeco.service.rest.json;

import java.util.List;

import org.sopeco.persistence.dataset.DataSetInputColumn;
import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MixIn for {@link DataSetInputColumn} class, which has no default constructor and some
 * getXYZ methods, where XYZ is no field.
 * 
 * @author Peter Merkert
 */
public final class DataSetInputColumnMixIn<T> {

	@JsonCreator
	protected DataSetInputColumnMixIn(@JsonProperty("parameter") ParameterDefinition parameter, @JsonProperty("valueList") List<T> values) {
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
}
