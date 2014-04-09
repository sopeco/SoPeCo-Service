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
package org.sopeco.service.helper;

import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
import org.sopeco.persistence.entities.definition.MeasurementSpecification;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ParameterNamespace;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * See {@link org.sopeco.persistence.EntityFactory} as reference. This utility
 * class provides some useful default setup methods for complex SoPeCo objects.
 * 
 * @author Marius Oehler
 * @author Peter Merkert
 */
public final class SimpleEntityFactory {

	private SimpleEntityFactory() {
	}

	/**
	 * Returns a default {@link MeasurementEnvironmentDefinition}. This should be used, as the default
	 * {@link MeasurementEnvironmentDefinition} should have a root {@link ParameterNamespace} with 
	 * the default name set in {@link ServiceConfiguration#MEASUREMENTENVIRONMENT_ROOTNAME}.
	 * 
	 * @return a default {@link MeasurementEnvironmentDefinition}
	 */
	public static MeasurementEnvironmentDefinition createDefaultMeasurementEnvironmentDefinition() {
		MeasurementEnvironmentDefinition med = new MeasurementEnvironmentDefinition();
		ParameterNamespace rootNamespace = SimpleEntityFactory.createNamespace(ServiceConfiguration.MEASUREMENTENVIRONMENT_ROOTNAME);
		med.setRoot(rootNamespace);
		return med;
	}
	
	/**
	 * Creates a {@link MeasurementSpecification} with the given name. A blank
	 * {@link MeasurementSpecification} is created and only the name set.
	 * 
	 * @param name	the name
	 * @return		the created {@link MeasurementSpecification}
	 */
	public static MeasurementSpecification createMeasurementSpecification(String name) {
		MeasurementSpecification ms = new MeasurementSpecification();
		ms.setName(name);
		return ms;
	}
	
	/**
	 * Creates a {@link ParameterNamespace} with the given name.
	 * 
	 * @param name	the {@link ParameterNamespace} name
	 * @return		the created {@link ParameterNamespace}
	 */
	public static ParameterNamespace createNamespace(String name) {
		ParameterNamespace child = new ParameterNamespace();
		child.setName(name);
		return child;
	}

	/**
	 * Creates a {@link ParameterDefinition} with the given attributes.
	 * 
	 * @param name	the name of the {@link ParameterDefinition}
	 * @param type	the type
	 * @param role	the role as {@link ParameterRole}
	 * @return		the created {@link ParameterDefinition}
	 */
	public static ParameterDefinition createParameterDefinition(String name, String type, ParameterRole role) {
		ParameterDefinition pd = new ParameterDefinition();
		pd.setName(name);
		pd.setRole(role);
		pd.setType(type);
		return pd;
	}
}
