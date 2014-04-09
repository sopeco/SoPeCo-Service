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
package org.sopeco.service.persistence.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.sopeco.config.IConfiguration;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;
import org.sopeco.service.execute.QueuedExperiment;

/**
 * 
 * @author Marius Oehler
 * 
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "getAllExperiments", query = "SELECT u FROM ScheduledExperiment u"),
		@NamedQuery(name = "getExperimentsByAccount", query = "SELECT s FROM ScheduledExperiment s WHERE s.account = :account") })
public class ScheduledExperiment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "active")
	private boolean active;

	@Column(name = "addedTime")
	private long addedTime;

	@Column(name = "lastExecutionTime")
	private long lastExecutionTime;

	@Column(name = "nextExecutionTime")
	private long nextExecutionTime;

	@Column(name = "properties")
	private Map<String, Object> properties = new HashMap<String, Object>();

	@Lob
	@Column(name = "scenarioDefinition")
	private ScenarioDefinition scenarioDefinition;

	@Column(name = "startTime")
	private long startTime;

	@Column(name = "account")
	private long account;

	@Column(name = "controllerUrl")
	private String controllerUrl;

	@Id
	@GeneratedValue
	private long id;

	@Column(name = "isRepeating")
	private boolean isRepeating;

	@Column(name = "label")
	private String label;

	@Column(name = "repeatDays")
	private String repeatDays;

	@Column(name = "repeatHours")
	private String repeatHours;

	@Column(name = "repeatMinutes")
	private String repeatMinutes;

	@Column(name = "durations")
	private List<Long> durations = new ArrayList<Long>();

	@Lob
	@ElementCollection
	@Column(name = "selectedExperiments")
	private List<String> selectedExperiments = new ArrayList<String>();

	public ScheduledExperiment() {
	}

	public QueuedExperiment createQueuedExperiment() {
		QueuedExperiment queuedExperiment = new QueuedExperiment(this);
		queuedExperiment.setTimeQueued(System.currentTimeMillis());
		return queuedExperiment;
	}

	public List<String> getSelectedExperiments() {
		return selectedExperiments;
	}

	public void setSelectedExperiments(List<String> selectedExperiments) {
		this.selectedExperiments = selectedExperiments;
	}

	public List<Long> getDurations() {
		return durations;
	}

	public void setDurations(List<Long> pDurations) {
		this.durations = pDurations;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(IConfiguration configuration) {
		properties.putAll(configuration.exportConfiguration());
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getAddedTime() {
		return addedTime;
	}

	public void setAddedTime(long addedTime) {
		this.addedTime = addedTime;
	}

	public long getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(long lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	public long getNextExecutionTime() {
		return nextExecutionTime;
	}

	public void setNextExecutionTime(long nextExecutionTime) {
		this.nextExecutionTime = nextExecutionTime;
	}

	public ScenarioDefinition getScenarioDefinition() {
		return scenarioDefinition;
	}

	public void setScenarioDefinition(ScenarioDefinition scenarioDefinition) {
		this.scenarioDefinition = scenarioDefinition;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getAccountId() {
		return account;
	}

	public void setAccountId(long account) {
		this.account = account;
	}

	public String getControllerUrl() {
		return controllerUrl;
	}

	public void setControllerUrl(String controllerUrl) {
		this.controllerUrl = controllerUrl;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isRepeating() {
		return isRepeating;
	}

	public void setRepeating(boolean isRepeating) {
		this.isRepeating = isRepeating;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRepeatDays() {
		return repeatDays;
	}

	public void setRepeatDays(String repeatDays) {
		this.repeatDays = repeatDays;
	}

	public String getRepeatHours() {
		return repeatHours;
	}

	public void setRepeatHours(String repeatHours) {
		this.repeatHours = repeatHours;
	}

	public String getRepeatMinutes() {
		return repeatMinutes;
	}

	public void setRepeatMinutes(String repeatMinutes) {
		this.repeatMinutes = repeatMinutes;
	}

	/**
	 * Calculates a unique the experiment key for this {@link ScheduledExperiment}.
	 * At the moment only take the hash code of this object.<br />
	 * 
	 * @return the experiment key
	 */
	public long getExperimentKey() {
		return hashCode();
	}
	
	/**
	 * Hash function which append all the variables to a string and
	 * creates the hash function via the string. String has a quite 
	 * good hash function enabled. <br />
	 * Of course, the execution times are ignored creating the hash
	 * code, as they change over time. In Addition the active state
	 * is not taken into account, as it changes over time, too.<br />
	 * To be honest, the properties are not inserted, too, as it is
	 * too complex to flatten the map. HashCode() for the map cannot
	 * be used as it is not the same value before and after persisting!
	 * <br />
	 * <br />
	 * This method is absolut null-safe. There is no possiblity thath this
	 * method fails, as it is a really important one.
	 * <br />
	 * <br />
	 * If a better hash function is required, if can just be created here.
	 */
	@Override
	public int hashCode() {
		
		String addedTime			= String.valueOf(this.addedTime);
		String account				= String.valueOf(this.account);
		String id					= String.valueOf(this.id);
		String isRepeating			= String.valueOf(this.isRepeating);
		String label				= "";
		String repeatDays			= "";
		String repeatHours			= "";
		String repeatMinutes		= "";
		String scenarioDefinition	= "";
		String controllerUrl		= "";
		String durations 			= "";
		String selectedExperiments 	= "";
		
		if (scenarioDefinition != null) {
			scenarioDefinition = this.scenarioDefinition.toString();
		}
		
		if (this.controllerUrl != null) {
			controllerUrl = this.controllerUrl;
		}
		
		if (this.label != null) {
			label = this.label;
		}
		
		if (this.repeatDays != null) {
			repeatDays = this.repeatDays;
		}
		
		if (this.repeatHours != null) {
			repeatHours = this.repeatHours;
		}
		
		if (this.repeatMinutes != null) {
			repeatMinutes = this.repeatMinutes;
		}
		
		if (this.durations != null) {
			
			for (long l : this.durations) {
				durations += String.valueOf(l);
			}
			
		}
		
		if (this.selectedExperiments != null) {
			
			for (String s : this.selectedExperiments) {
				selectedExperiments += s;
			}
			
		}
		
		return (addedTime
				+ account
				+ id
				+ isRepeating
				+ label
				+ repeatDays
				+ repeatHours
				+ repeatMinutes
				+ scenarioDefinition
				+ controllerUrl
				+ durations
				+ selectedExperiments).hashCode();
	}
	
	
	/**
	 * Most of the time, the requester does not have the unique
	 * id for a ScheduledExperiment. This method is used to
	 * compare two ScheduledExperiments.
	 * 
	 * Compared are:
	 * - time added (mostly the unique idetification)
	 * - repeating
	 * - account id
	 * - activ status
	 * - controller url
	 * - label
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof ScheduledExperiment) {
			
			ScheduledExperiment se = (ScheduledExperiment) obj;	
			
			return se.addedTime == this.addedTime
					&& se.isRepeating == this.isRepeating
					&& se.account == this.account
					&& se.active == this.active
					&& se.controllerUrl.equals(this.controllerUrl)
					&& se.label.equals(this.label);
		}
		
		return false;
		
	}
}
