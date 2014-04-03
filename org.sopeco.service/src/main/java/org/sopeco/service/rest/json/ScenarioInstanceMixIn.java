package org.sopeco.service.rest.json;

import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ScenarioInstance;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * This Jackson MixIn class is used in the {@link CustomObjectMapper} to mix some Json annotations
 * into the {@link ScenarioInstance} class. These annotations are needed to (de)serialize the class
 * correctly.<br />
 * As the {@link ScenarioInstance} has a List of {@link ExperimentSeries}, and each {@link ExperimentSeries}
 * has a {@link ScenarioInstance}, we need for the recusion the {@link JsonIdentityInfo}.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public final class ScenarioInstanceMixIn {

	@JsonIgnore
	public String getName() {
		return "";
	}

	@JsonIgnore
	public String getMeasurementEnvironmentUrl() {
		return "";
	}

}
