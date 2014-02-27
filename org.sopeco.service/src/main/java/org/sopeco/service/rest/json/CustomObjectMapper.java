package org.sopeco.service.rest.json;

import org.sopeco.persistence.entities.definition.ParameterNamespace;
import org.sopeco.service.persistence.entities.AccountDetails;
import org.sopeco.service.persistence.entities.ScheduledExperiment;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is used to customize the used Jackson Json converter. E.g. say that the conversation must
 * not fail when there are unknown properties. 
 * The example above happened, whe trying to map the {@code ParameterNamespace} from SoPeCo Core, because
 * a method called "getFullName" exists, but not the field "fullName". The Jackson Json parsing provider
 * throw an error and shut down. To prevent this behaviour we need to tell Jackson to ingore these "not
 * mappable properties".
 * 
 * Needed to fix the class {@code ParameterNamespace} to be serialized and deserialized with self
 * references. To do so, we needed to add a Jackson Annotation for the class {@code ParameterNamespace}.
 * As we are not allowed to change the class code itself, we use a MixIn via Jackson to inject our
 * Jackson annotation for correct JSON converting with self-references.
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
		addMixInAnnotations(ScheduledExperiment.class, ScheduledExperimentMixIn.class);
		addMixInAnnotations(AccountDetails.class, AccountDetailsMixIn.class);
	 }
}
