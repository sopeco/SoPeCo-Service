package org.sopeco.service.rest.json;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * This class is only used to inject the class {@link org.sopeco.persistence.entities.definition.ParameterNamespace}
 * with a Jackson annotation. For more information have a look at {@code CustomObjectWrapper}.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public final class ParameterNamespaceMixIn {

}
