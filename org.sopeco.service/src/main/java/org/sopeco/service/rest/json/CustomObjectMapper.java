package org.sopeco.service.rest.json;

import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.DataSetInputColumn;
import org.sopeco.persistence.dataset.DataSetObservationColumn;
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
		//addMixInAnnotations(AbstractDataSetColumn.class, AbstractDataSetColumnMixIn.class);
	 }
}
