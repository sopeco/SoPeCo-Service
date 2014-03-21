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
package org.sopeco.service.builder;

import java.io.Serializable;

import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ScenarioDefinition;

/**
 * Builds a {@link ScenarioDefinition}. This class handles the two {@link ScenarioDefinition}
 * properties: {@link MeasurementEnvironmentDefinition} and  List<{@link MeasurementSpecification}>.
 * <br />
 * Both properties are updates via so called builder classes ({@link MeasurementEnvironmentDefinitionBuilder}
 * and {@link MeasurementSpecificationBuilder}).
 * 
 * @author Marius Oehler
 * @author Peter Merkert
 */
public class ScenarioDefinitionBuilder implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The {@link ScenarioDefinition}
	 */
	private ScenarioDefinition scenarioDefinition;
	
	/**
	 * The {@link MeasurementEnvironmentDefinitionBuilder} to handle the {@link MeasurementEnvironmentDefinition}
	 * of the connected {@link ScenarioDefinition}.
	 */
	private MeasurementEnvironmentDefinitionBuilder meBuilder;
	
	/**
	 * The {@link MeasurementSpecificationBuilder} to handle (the user's active selected)
	 * {@link MeasurementSpecification} in the connected {@link ScenarioDefinition}.
	 */
	private MeasurementSpecificationBuilder msBuilder;

	/**
	 * Creates an empty {@link ScenarioDefinition} and initializes the two sub-builders:
	 * {@link MeasurementEnvironmentDefinitionBuilder} and {@link MeasurementSpecificationBuilder}.
	 */
	public ScenarioDefinitionBuilder() {
		scenarioDefinition = new ScenarioDefinition();
		meBuilder = new MeasurementEnvironmentDefinitionBuilder(this);
		msBuilder = new MeasurementSpecificationBuilder(this);
	}
	
	/**
	 * Creates an empty {@link ScenarioDefinition} with the given nameand initializes the two sub-builders:
	 * {@link MeasurementEnvironmentDefinitionBuilder} and {@link MeasurementSpecificationBuilder}.
	 * 
	 * @param name 	name of the new {@link ScenarioDefinition}
	 */
	public ScenarioDefinitionBuilder(String name) {
		scenarioDefinition = new ScenarioDefinition();
		meBuilder = new MeasurementEnvironmentDefinitionBuilder(this);
		msBuilder = new MeasurementSpecificationBuilder(this);
		
		scenarioDefinition.setScenarioName(name);
	}
	
	/**
	 * Connects the {@link ScenarioDefinition} of this class with the given one.<br />
	 * Initializes the two sub-builders: {@link MeasurementEnvironmentDefinitionBuilder}
	 * and {@link MeasurementSpecificationBuilder}.
	 * 
	 * @param definition the {@link ScenarioDefinition}
	 */
	public ScenarioDefinitionBuilder(ScenarioDefinition definition) {
		scenarioDefinition = definition;
		meBuilder = new MeasurementEnvironmentDefinitionBuilder(this);
		msBuilder = new MeasurementSpecificationBuilder(this);
	}

	/**
	 * Sets the name of the {@link ScenarioDefinition}.
	 * 
	 * @param name new scenario name
	 */
	public void setScenarioName(String name) {
		scenarioDefinition.setScenarioName(name);
	}

	/**
	 * @return a MeasurementEnvironmentDefinitionBuilder
	 * 
	 * @Deprecated Use the method getMeasurementEnvironmentBuilder().
	 */
	@Deprecated
	public MeasurementEnvironmentDefinitionBuilder getEnvironmentBuilder() {
		return getMeasurementEnvironmentBuilder();
	}
	
	/**
	 * Returns the builder for the measurement environemnt.
	 * 
	 * @return the measurement environment builder
	 */
	public MeasurementEnvironmentDefinitionBuilder getMeasurementEnvironmentBuilder() {
		return meBuilder;
	}

	/**
	 * @param meDefinition the MeasurementEnvironmentDefinition
	 * 
	 * @deprecated Use {@link #setMeasurementEnvironmentDefinition(MeasurementEnvironmentDefinition)}
	 */
	@Deprecated
	public void setMEDefinition(MeasurementEnvironmentDefinition meDefinition) {
		setMeasurementEnvironmentDefinition(meDefinition);
	}
	
	/**
	 * Sets the {@link MeasurementEnvironmentDefinition} for the connected {@link ScenarioDefinition}
	 * of this builder.
	 * 
	 * @param meDefinition the new {@link MeasurementEnvironmentDefinition}
	 */
	public void setMeasurementEnvironmentDefinition(MeasurementEnvironmentDefinition meDefinition) {
		scenarioDefinition.setMeasurementEnvironmentDefinition(meDefinition);
	}

	/**
	 * @return a MeasurementEnvironmentDefinition 
	 * 
	 * @Deprecated Use getMeasurementEnvironmentDefinition()
	 */
	@Deprecated
	public MeasurementEnvironmentDefinition getMEDefinition() {
		return getMeasurementEnvironmentDefinition();
	}

	/**
	 * Returns the current {@link MeasurementEnvironmentDefinition}.
	 * 
	 * @return the current {@link MeasurementEnvironmentDefinition}
	 */
	public MeasurementEnvironmentDefinition getMeasurementEnvironmentDefinition() {
		return scenarioDefinition.getMeasurementEnvironmentDefinition();
	}
	
	/**
	 * @return a MeasurementSpecificationBuilder
	 * 
	 * @Deprecated use {@link #getNewMeasurementSpecificationBuilder()}
	 */
	@Deprecated
	public MeasurementSpecificationBuilder addNewMeasurementSpecification() {
		return getNewMeasurementSpecificationBuilder();
	}

	/**
	 * Returns an empty fresh created {@link MeasurementSpecificationBuilder}.
	 * 
	 * @return empty created MeasurementSpecification
	 */
	public MeasurementSpecificationBuilder getNewMeasurementSpecificationBuilder() {
		MeasurementSpecificationBuilder msb = new MeasurementSpecificationBuilder(this);
		return msb;
	}

	/**
	 * Returns the {@link MeasurementSpecification} with the given name.
	 * 
	 * @param name 	name of the specification
	 * @return 		{@link MeasurementSpecification} for the given name (<code>null</code> when not found)
	 */
	public MeasurementSpecification getMeasurementSpecification(String name) {
		for (MeasurementSpecification ms : getScenarioDefinition().getMeasurementSpecifications()) {
			if (ms.getName().equals(name)) {
				return ms;
			}
		}
		
		return null;
	}

	/**
	 * @param builder the MeasurementSpecificationBuilder
	 * 
	 * @Deprecated Use setMeasurementSpecificationBuilder().
	 */
	@Deprecated
	public void setSpecificationBuilder(MeasurementSpecificationBuilder builder) {
		setMeasurementSpecificationBuilder(builder);
	}
	
	/**
	 * Replaces the current {@link MeasurementSpecificationBuilder} with the given
	 * new one.
	 * 
	 * @param builder the new {@link MeasurementSpecificationBuilder}
	 */
	public void setMeasurementSpecificationBuilder(MeasurementSpecificationBuilder builder) {
		msBuilder = builder;
	}

	/**
	 * @return a MeasurementSpecificationBuilder 
	 * 
	 * @Deprecated Use getMeasurementSpecificationBuilder().
	 */
	@Deprecated
	public MeasurementSpecificationBuilder getSpecificationBuilder() {
		return getMeasurementSpecificationBuilder();
	}

	/**
	 * Returns the builder for the default {@Code MeasurementSpecification}.
	 * 
	 * @return the measurement specification builder
	 */
	public MeasurementSpecificationBuilder getMeasurementSpecificationBuilder() {
		return msBuilder;
	}
	
	/**
	 * @return a ScenarioDefinition
	 * 
	 * @Deprecated Use getScenarioDefinition().
	 */
	@Deprecated
	public ScenarioDefinition getBuiltScenario() {
		return getScenarioDefinition();
	}
	
	/**
	 * Returns the {@link ScenarioDefinition} which this {@link ScenarioDefinitionBuilder}
	 * is handling.
	 * 
	 * @return the {@link ScenarioDefinition}
	 */
	public ScenarioDefinition getScenarioDefinition() {
		return scenarioDefinition;
	}

	/**
	 * @param name the name
	 * @return a ScenarioDefinition
	 * 
	 * @Deprecated Use constructor and getScenarioDefinition().
	 */
	@Deprecated
	public static ScenarioDefinition buildEmptyScenario(String name) {
		ScenarioDefinitionBuilder builder = new ScenarioDefinitionBuilder();

		builder.setScenarioName(name);

		return builder.getBuiltScenario();
	}

	/**
	 * @param definition a ScenarioDefinition
	 * @return a ScenarioDefinitionBuilder
	 * 
	 * @Deprecated Use constructor.
	 */
	@Deprecated
	public static ScenarioDefinitionBuilder load(ScenarioDefinition definition) {
		ScenarioDefinitionBuilder builder = new ScenarioDefinitionBuilder();
		builder.scenarioDefinition = definition;
		return builder;
	}
}
