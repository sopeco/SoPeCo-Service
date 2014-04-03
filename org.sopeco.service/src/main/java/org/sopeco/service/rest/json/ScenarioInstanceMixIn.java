package org.sopeco.service.rest.json;

import java.util.List;

import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ScenarioInstance;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This Jackson MixIn class is used in the {@link CustomObjectMapper} to mix some Json annotations
 * into the {@link ScenarioInstance} class. These annotations are needed to (de)serialize the class
 * correctly.
 * 
 * @author Peter Merkert
 */
public final class ScenarioInstanceMixIn {

	@JsonIgnore
	public String getName() {
		return "";
	}

	@JsonIgnore
	public String getMeasurementEnvironmentUrl() {
		return "";
	}

	@JsonIgnore
	public List<ExperimentSeries> getExperimentSeriesList() {
		return null;
	}
	
}
