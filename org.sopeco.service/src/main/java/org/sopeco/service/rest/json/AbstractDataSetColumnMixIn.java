package org.sopeco.service.rest.json;

import org.sopeco.persistence.dataset.AbstractDataSetColumn;
import org.sopeco.persistence.dataset.DataSetInputColumn;
import org.sopeco.persistence.dataset.DataSetObservationColumn;
import org.sopeco.persistence.entities.definition.ParameterDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * MixIn for {@link AbstractDataSetColumn} to respect inheritance in JSON parsing.
 * 
 * @author Peter Merkert
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=DataSetInputColumn.class, name="dataSetInputColumn"),
	@JsonSubTypes.Type(value=DataSetObservationColumn.class, name="dataSetObservationColumn")
})
public abstract class AbstractDataSetColumnMixIn {

	@JsonProperty("parameter")
	private ParameterDefinition parameter;
	
}
