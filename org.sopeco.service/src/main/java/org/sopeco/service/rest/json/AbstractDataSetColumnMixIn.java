package org.sopeco.service.rest.json;

import org.sopeco.persistence.dataset.AbstractDataSetColumn;

/**
 * MixIn for {@link AbstractDataSetColumn} to respect inheritance in JSON parsing.
 * 
 * @author Peter Merkert
 */
/*@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=DataSetInputColumn.class, name="dataSetInputColumn"),
	@JsonSubTypes.Type(value=DataSetObservationColumn.class, name="dataSetObservationColumn")
}) */
public abstract class AbstractDataSetColumnMixIn {
}
