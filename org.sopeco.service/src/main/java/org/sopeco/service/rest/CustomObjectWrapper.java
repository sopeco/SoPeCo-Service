package org.sopeco.service.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.sopeco.persistence.entities.definition.ParameterNamespace;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is used to customize the used Jackson Json converter. E.g. say that the conversation must
 * not fail when there are unknown properties. 
 * The example above happened, whe trying to map the {@code ParameterNamespace} from SoPeCo Core, because
 * a method called "getFullName" exists, but not the field "fullName". The Jackson Json parsing provider
 * throw an error and shut down. To prevent this behaviour we need to tell Jackson to ingore these "not
 * mappable properties".
 * 
 * @author Peter Merkert
 */
@Provider
public class CustomObjectWrapper implements ContextResolver<ObjectMapper> {

	private ObjectMapper om;
	
	 public CustomObjectWrapper() {
		 om = new ObjectMapper();
		 om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		 
		 //om.addMixInAnnotations(arg0, arg1);
		 
		 // mixin for ParameterNamespace
		 // http://stackoverflow.com/questions/10937924/cant-get-a-basic-jackson-mixin-to-work
		 //om.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.PROTECTED_AND_PUBLIC));
		 //om.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(Visibility.ANY));
		 //om.getSerializationConfig().addMixInAnnotations(ParameterNamespace.class, ParameterNamespaceMixIn.class);
		 //om.getDeserializationConfig().addMixInAnnotations(ParameterNamespace.class, ParameterNamespaceMixIn.class);
	 }

	public ObjectMapper getContext(Class<?> objectType) {
		return om;
	}
}
