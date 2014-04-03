package org.sopeco.service.rest.json;

import java.util.ArrayList;
import java.util.List;

import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.persistence.entities.keys.ScenarioInstancePK;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
	
	@JsonProperty("description")
	private String description;

	@JsonIgnore
	private List<ExperimentSeries> experimentSeriesList = new ArrayList<ExperimentSeries>();

	@JsonProperty("scenarioDefinition")
	private ScenarioDefinition scenarioDefinition;

	@JsonProperty("primaryKey")
	private ScenarioInstancePK primaryKey = new ScenarioInstancePK();
	
	@JsonIgnore
	public String getName() {
		return "";
	}

	@JsonIgnore
	public String getMeasurementEnvironmentUrl() {
		return "";
	}

}
