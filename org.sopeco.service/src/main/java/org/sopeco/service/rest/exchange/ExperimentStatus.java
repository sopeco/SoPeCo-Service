/**
 * Copyright (c) 2013 SAP
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
package org.sopeco.service.rest.exchange;

import java.util.ArrayList;
import java.util.List;

import org.sopeco.service.execute.MECLogEntry;

/**
 * The {@link ExperimentStatus} comprises all relevant information for an scenario
 * in execution.
 * 
 * @author Peter Merkert
 */
public class ExperimentStatus {

	private long timeStart					= -1l;		
	private long timeRemaining				= -1l;
	private String label					= "";
	private long accountId					= -1l;
	private String scenarioName				= "";
	private float progress					= 0.0f;
	private List<MECLogEntry> eventLogList 	= new ArrayList<MECLogEntry>();
	private boolean finished 				= false;

	/**
	 * Returns the event log list of this experiment.
	 * 
	 * @return the event log list
	 */
	public List<MECLogEntry> getEventLogList() {
		return eventLogList;
	}

	/**
	 * Sets the event log list with the given list of {@link MECLogEntry}s.
	 * 
	 * @param eventLogList the log list to set
	 */
	public void setEventLogList(List<MECLogEntry> eventLogList) {
		this.eventLogList = eventLogList;
	}

	/**
	 * Returns true, if this experiment has finished.
	 * 
	 * @return true, if this experiment has finished
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Set the finish parameter.
	 * 
	 * @param finished if the experiment is finished
	 */
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
	 * Returns the time when the experiment has started the last time.
	 * 
	 * @return the time when started as long
	 */
	public long getTimeStart() {
		return timeStart;
	}

	/**
	 * Sets the start time.
	 * 
	 * @param timeStart the start time
	 */
	public void setTimeStart(long timeStart) {
		this.timeStart = timeStart;
	}

	/**
	 * Returns the remeining time for the experiment.
	 * 
	 * @return the remeining time for the experiment
	 */
	public long getTimeRemaining() {
		return timeRemaining;
	}

	/**
	 * Sets the remaining time for the experiment.
	 * 
	 * @param timeRemaining the remeining time
	 */
	public void setTimeRemaining(long timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	/**
	 * Returns the ID of the related account for this experiment.
	 * 
	 * @return the account id
	 */
	public long getAccountId() {
		return accountId;
	}

	/**
	 * Sets the account ID of the related account.
	 * 
	 * @param account the account ID to set
	 */
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	/**
	 * Returns the name of the scenario on which the experiment is executed.
	 * 
	 * @return the name of the scenario on which the experiment is executed
	 */
	public String getScenarioName() {
		return scenarioName;
	}

	/**
	 * Sets the scenario name on which this experiment is executed.
	 * 
	 * @param scenario the scenario name
	 */
	public void setScenarioName(String scenario) {
		this.scenarioName = scenario;
	}

	/**
	 * Returns the progress of this experiment.
	 * 
	 * @return the progress of this experiment
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * Sets the progress of this experiment.
	 * 
	 * @param progress the progress
	 */
	public void setProgress(float progress) {
		this.progress = progress;
	}

	/**
	 * Returns the label of this experiment.
	 * 
	 * @return the label of this experiment
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label of the experiment.
	 * 
	 * @param label the label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}
