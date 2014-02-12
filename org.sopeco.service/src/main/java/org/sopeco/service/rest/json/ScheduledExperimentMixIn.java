package org.sopeco.service.rest.json;

import org.sopeco.config.IConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class' only task is to provide a JSON annotation for a set method.
 * The method setProperties(IConfiguration configuration) was the default
 * setter method in the class {@link ScheduledExperiment}. Jackson tried to
 * use the setter with instantiating an object of interface type
 * {@link IConfiguration} which led to a Java exception.<br />
 * To tell Jackson, to ignore this method the annotation is used. As it is not
 * possible to manipulate the original <code>ScheduledExperiment</code> class
 * this MixIn is used.
 * 
 * @author Peter Merkert
 */
public final class ScheduledExperimentMixIn {

	/**
	 * The setProperties method must be ignore by Json, because otherwise it
	 * tries to call the method with an object of type {@link IConfiguration}.
	 * This is going to fail, because <code>IConfiguration</code> is an
	 * interface and cannot be instantiated.
	 * 
	 * @param configuration the configuration
	 */
	@JsonIgnore
	public void setProperties(IConfiguration configuration) {
	}
	
	/**
	 * The method is used for a better acces to the experimentkey. The original
	 * key is calculated via the hash code of the {@link ScheduledExperiment} and
	 * therefor is no class field. But Jackson tries to may this getter to a 
	 * property of the class and fails.<br />
	 * A <code>@JsonIgnore</code> is needed.
	 * 
	 * @return the experiment key
	 */
	@JsonIgnore
	public int getExperimentKey() {
		return 0;
	}
	
}
