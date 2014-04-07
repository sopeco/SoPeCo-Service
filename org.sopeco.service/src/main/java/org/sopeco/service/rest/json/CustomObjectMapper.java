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

import org.sopeco.persistence.dataset.AbstractDataSetColumn;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.DataSetInputColumn;
import org.sopeco.persistence.dataset.DataSetObservationColumn;
import org.sopeco.persistence.dataset.DataSetRow;
import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.ParameterValueList;
import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ParameterNamespace;
import org.sopeco.persistence.entities.definition.ParameterValueAssignment;
import org.sopeco.service.persistence.entities.ScheduledExperiment;
import org.sopeco.service.rest.exchange.ExperimentSeriesRunDecorator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is used to customize the used Jackson Json converter. E.g. say that some methods should be
 * ignored when serializing.<br />
 * The example above happened, whe trying to map the {@code ParameterNamespace} from SoPeCo Core, because
 * a method called "getFullName" exists, but not the field "fullName". The Jackson Json parsing provider
 * throws an error and shuts down, but actually we just want the parser to ignore the automatically (wrong)
 * identified field "fullName" to be ignored.
 * 
 * @author Peter Merkert
 */
public class CustomObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The constructor for the CustomObjectWrapper. The default configuration is set up here.
	 * <br />
	 * In this case the MixIn classes are injected into the {@link ObjectMapper}.
	 */
	public CustomObjectMapper() {
		// mixin for ParameterNamespace, to have Jackson annotation from ParameterNamespaceMixIn
		addMixInAnnotations(ParameterNamespace.class, ParameterNamespaceMixIn.class);
		addMixInAnnotations(ParameterDefinition.class, ParameterDefinitionMixIn.class);
		addMixInAnnotations(ScheduledExperiment.class, ScheduledExperimentMixIn.class);
		addMixInAnnotations(ParameterValueAssignment.class, ParameterValueAssignmentMixIn.class);
		
		// the following MixIns are only needed to pass a ScenarioInstance with JSON
		addMixInAnnotations(ScenarioInstance.class, ScenarioInstanceMixIn.class);
		addMixInAnnotations(DataSetInputColumn.class, DataSetInputColumnMixIn.class);
		addMixInAnnotations(ParameterValue.class, ParameterValueMixIn.class);
		addMixInAnnotations(DataSetObservationColumn.class, DataSetObservationColumnMixIn.class);
		addMixInAnnotations(ParameterValueList.class, ParameterValueListMixIn.class);
		addMixInAnnotations(ExperimentSeries.class, ExperimentSeriesMixIn.class);
		addMixInAnnotations(ExperimentSeriesRun.class, ExperimentSeriesRunMixIn.class);
		addMixInAnnotations(ExperimentSeriesRunDecorator.class, ExperimentSeriesRunDecoratorMixIn.class);
		
		addMixInAnnotations(DataSetAggregated.class, DataSetAggregatedMixIn.class);
		addMixInAnnotations(ParameterValueList.class, ParameterValueListMixIn.class);
		addMixInAnnotations(DataSetRow.class, DataSetRowMixIn.class);
		addMixInAnnotations(AbstractDataSetColumn.class, AbstractDataSetColumnMixIn.class);
	 }
}
