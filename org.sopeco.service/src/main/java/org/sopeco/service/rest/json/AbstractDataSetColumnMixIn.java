package org.sopeco.service.rest.json;

import org.sopeco.persistence.dataset.DataSetInputColumn;
import org.sopeco.persistence.dataset.DataSetObservationColumn;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * MixIn for {@link DataSetInputColumn} class, which has no default constructor and some
 * getXYZ methods, where XYZ is no field.
 * 
 * @author Peter Merkert
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=DataSetInputColumn.class, name="dataSetInputColumn"),
	@JsonSubTypes.Type(value=DataSetObservationColumn.class, name="dataSetObservationColumn")
}) 
public abstract class AbstractDataSetColumnMixIn {
}
