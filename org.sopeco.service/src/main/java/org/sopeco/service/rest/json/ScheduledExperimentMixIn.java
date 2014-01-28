package org.sopeco.service.rest.json;

import org.sopeco.config.IConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class' only task is to provide a JSON annotation for a set method.
 * The method setProperties(IConfiguration configuration) was the default
 * setter method in the class {@code ScheduledExperiment}. Jackson tried to
 * use the setter with instantiating an object of interface type
 * {@code IConfiguration} which led to a Java exception.<br />
 * To tell Jackson, to ignore this method the annotation is used. As it is not
 * possible to manipulate the original {@code ScheduledExperiment} class
 * this MixIn is used.
 * 
 * @author Peter Merkert
 */
public final class ScheduledExperimentMixIn {

	@JsonIgnore
	public void setProperties(IConfiguration configuration) {
	}
	
}
