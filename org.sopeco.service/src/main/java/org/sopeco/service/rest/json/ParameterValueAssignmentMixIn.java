package org.sopeco.service.rest.json;

import org.sopeco.persistence.entities.definition.ConstantValueAssignment;
import org.sopeco.persistence.entities.definition.DynamicValueAssignment;
import org.sopeco.persistence.entities.definition.ParameterValueAssignment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This MixIn is needed for method in the original {@link ParameterValueAssignment}, which
 * need annotation for fasterxml converting it into a default "creatable" object. The 
 * {@link ParameterValueAssignment} class is abstract and does not provide the possiblity
 * to pass it easily with JSON.<br />
 * <br />
 * Furhter resources:<br />
 * http://wiki.fasterxml.com/JacksonFAQ#Data_Binding.2C_reading_.28de-serialization.29<br />
 * http://wiki.fasterxml.com/JacksonPolymorphicDeserialization<br />
 * http://stackoverflow.com/questions/5826928/how-can-i-prevent-jackson-from-serializing-a-polymorphic-types-annotation-prope
 * 
 * @author Peter Merkert
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=DynamicValueAssignment.class, name="dynamicValueAssignment"),
	@JsonSubTypes.Type(value=ConstantValueAssignment.class, name="constantValueAssignment")
}) 
public class ParameterValueAssignmentMixIn {

}