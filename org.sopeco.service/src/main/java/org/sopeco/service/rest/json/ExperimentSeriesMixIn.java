package org.sopeco.service.rest.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.DataSetAppender;
import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.ProcessedDataSet;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.exceptions.ExperimentFailedException;
import org.sopeco.persistence.entities.keys.ExperimentSeriesPK;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Just another MixIn for method which cause problems when (de)serializing.
 * 
 * @author Peter Merkert
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public final class ExperimentSeriesMixIn<T> {

	@JsonIgnore
	public String getName() {
		return null;
	}

	@JsonIgnore
	public void setName(String name) {
	}

	public ScenarioInstance getScenarioInstance() {
		return null;
	}

	public void setScenarioInstance(ScenarioInstance scenarioInstance) {

	}

	public ExperimentSeriesDefinition getExperimentSeriesDefinition() {

		return null;
	}

	public void setExperimentSeriesDefinition(
			ExperimentSeriesDefinition experimentSeriesDefinition) {

	}

	public List<ExperimentSeriesRun> getExperimentSeriesRuns() {
		return null;
	}

	public List<ProcessedDataSet> getProcessedDataSets() {
		return null;
	}

	@JsonIgnore
	public void addProcessedDataSet(ProcessedDataSet pds) {

	}

	public ExperimentSeriesPK getPrimaryKey() {
		return null;
	}

	@JsonIgnore
	public DataSetAggregated getAllExperimentSeriesRunSuccessfulResultsInOneDataSet() {
		DataSetAppender appender = new DataSetAppender();
		for (ExperimentSeriesRun seriesRun : getExperimentSeriesRuns()) {
			if (seriesRun.getSuccessfulResultDataSet() != null) {
				appender.append(seriesRun.getSuccessfulResultDataSet());
			}
		}

		return appender.createDataSet();
	}

	@JsonIgnore
	public List<ExperimentFailedException> getAllExperimentFailedExceptions() {
		List<ExperimentFailedException> result = new ArrayList<ExperimentFailedException>();

		for (ExperimentSeriesRun seriesRun : getExperimentSeriesRuns()) {
			final List<ExperimentFailedException> seriesExceptions = seriesRun
					.getExperimentFailedExceptions();
			if (seriesExceptions != null) {
				result.addAll(seriesExceptions);
			}
		}

		return result;
	}

	@JsonIgnore
	public ExperimentSeriesRun getLatestExperimentSeriesRun() {
		if (this.getExperimentSeriesRuns().size() > 0) {
			// sorts the list of experiment runs descending based on their
			// timestamps
			Collections.sort(this.getExperimentSeriesRuns());

			return this.getExperimentSeriesRuns().get(0);
		} else {
			return null;
		}
	}

}
