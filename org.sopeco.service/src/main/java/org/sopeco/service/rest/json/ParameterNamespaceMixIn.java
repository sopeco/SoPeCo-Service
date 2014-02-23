package org.sopeco.service.rest.json;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * This interface is only used to inject the class {@link org.sopeco.persistence.entities.definition.ParameterNamespace}
 * with a Jackson annotation. The injection is done via Jackson MixIns.
 * <br /><br />
 * The interface annotation with {@code @JsonIdentityInfo} is used to enable a cyclic
 * reference in this class in serializing+deserializing. Jackson normally does not recognize
 * cyclic references as it does not check for the same reference. With a special ID field,
 * Jackson is able to set references to unique IDs. With these IDs Jackson checks for cyclic
 * references.
 * <br /><br />
 * The annotation {@code @JsonIgnore} at the method {@code getFullName()} is used to tell Jackson
 * that this method is no getter for a class/interface field. Otherwise Jackson searches for
 * a field named {@code fullName}, which is not available.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public interface ParameterNamespaceMixIn {

	/**
	 * The annotation {@code @JsonIgnore} at the method {@code getFullName()} is used to tell Jackson
	 * that this method is no getter for a class/interface field. Otherwise Jackson searches for
	 * a field named {@code fullName}, which is not available.
	 * 
	 * @return 	the full namespace string
	 */
	@JsonIgnore
	String getFullName();
	
}
