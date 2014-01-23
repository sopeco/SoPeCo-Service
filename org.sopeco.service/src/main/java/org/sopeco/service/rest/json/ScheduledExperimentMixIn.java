package org.sopeco.service.rest.json;

import org.sopeco.config.IConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class ScheduledExperimentMixIn {

	@JsonIgnore
	public void setProperties(IConfiguration configuration) {
	}

	
}
