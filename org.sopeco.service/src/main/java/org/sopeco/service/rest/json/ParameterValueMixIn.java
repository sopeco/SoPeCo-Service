package org.sopeco.service.rest.json;

import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Just another MixIn (for class {@link ParameterValue}) for method which cause problems when (de)serializing.
 * 
 * @author Peter Merkert
 */
public final class ParameterValueMixIn<T> {

	@JsonProperty("TOLERANCE_VALUE")
	private static final double TOLERANCE_VALUE = 0.0000000000000001;

	@JsonProperty("parameter")
	private ParameterDefinition parameter;
	
	@JsonCreator
	protected ParameterValueMixIn(@JsonProperty("parameter") ParameterDefinition parameter, @JsonProperty("value") T value) {
	}
	
	@JsonIgnore
	public String getValueAsString() {
		return "";
	}
	
	@JsonIgnore
	public double getValueAsDouble() {
		return 0.0f;
	}
	
	@JsonIgnore
	public boolean getValueAsBoolean() {
		return false;
	}
	
	@JsonIgnore
	public int getValueAsInteger() {
		return 0;
	}
}
