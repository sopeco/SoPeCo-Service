/**
 * Copyright (c) 2014 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.service.rest.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.DataSetAppender;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.ProcessedDataSet;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
import org.sopeco.persistence.entities.exceptions.ExperimentFailedException;
import org.sopeco.persistence.entities.keys.ExperimentSeriesPK;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
