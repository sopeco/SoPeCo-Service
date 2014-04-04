package org.sopeco.service.rest.json;

import java.util.List;

import org.sopeco.persistence.dataset.DataSetRow;
import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.ParameterValueList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MixIn for {@link DataSetRow} class for properties and methods.
 * 
 * @author Peter Merkert
 */
public final class DataSetRowMixIn<T> {

	@JsonProperty("inputParameterValues")
	private List<ParameterValue<?>> inputParameterValues;

	@JsonProperty("observationParameterValues")
	private List<ParameterValueList<?>> observationParameterValues;

	@JsonIgnore
	public List<ParameterValue<?>> getInputRowValues() {
		return null;
	}

	@JsonIgnore
	public List<ParameterValueList<?>> getObservableRowValues() {
		return null;
	}

	@JsonIgnore
	public int getMaxSize(){
		return 0;
	}

}
