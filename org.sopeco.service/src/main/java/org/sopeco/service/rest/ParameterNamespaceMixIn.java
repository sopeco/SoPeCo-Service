package org.sopeco.service.rest;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.sopeco.persistence.entities.definition.ParameterNamespace;

public class ParameterNamespaceMixIn {
	
	@JsonBackReference("role-User")
	protected List<ParameterNamespace> children = new ArrayList<ParameterNamespace>();
	
	@JsonManagedReference("role-User")
	protected ParameterNamespace parent;
	
}
