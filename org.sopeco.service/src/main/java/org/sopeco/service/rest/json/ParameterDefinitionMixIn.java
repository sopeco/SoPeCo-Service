package org.sopeco.service.rest.json;

import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This MixIn is needed for method in the original {@link ParameterDefinition}, which
 * need annotation for fasterxml converting.
 * 
 * @author Peter Merkert
 */
public class ParameterDefinitionMixIn {

	@JsonIgnore
	public boolean isNumeric() {
		return false;
	}
	
}