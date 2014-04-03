package org.sopeco.service.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Just another MixIn for method which cause problems when (de)serializing.
 * 
 * @author Peter Merkert
 */
public final class ParameterValueMixIn {

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
