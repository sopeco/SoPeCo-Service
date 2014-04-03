package org.sopeco.service.rest.json;

import java.util.List;

import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Just another MixIn for method which cause problems when (de)serializing.
 * 
 * @author Peter Merkert
 */
public final class ParameterValueListMixIn<T> {
	
	@JsonCreator
	public ParameterValueListMixIn(@JsonProperty("parameter") ParameterDefinition parameter, @JsonProperty("values") List<T> values) {
	}
	
	@JsonIgnore
	public double getMean() {
		return 0.0f;
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
