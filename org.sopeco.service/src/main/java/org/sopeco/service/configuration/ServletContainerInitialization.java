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
package org.sopeco.service.configuration;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.sopeco.service.rest.json.CustomObjectMapper;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * This class is registered as an {@link Application} class at the Jersey web.xml settings
 * and is called once the {@link ServletContainer} is started up. The class is needed to register the custom
 * {@link JacksonJaxbJsonProvider} to the RESTful service, to get the {@link CustomObjectMapper}. It's also
 * needed to register the {@link ServletContainerLifecycleListener} to listen for changes
 * in the servlet context.
 * <br />
 * <br />
 * For more information, please visit
 * <a href="https://jersey.java.net/documentation/latest/deployment.html#d0e2747">Jersey SPI ResourceConfig
 * registration</a>.
 * 
 * @author Peter Merkert
 */
public class ServletContainerInitialization extends ResourceConfig {
	
	/**
	 * The constructor is used to register the {@link CustomObjectMapper} in the {@link Application}. And
	 * it's used to register the {@link ServletContainerLifecycleListener}.
	 */
    public ServletContainerInitialization() {
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());
		register(provider);
		
		packages(ServiceConfiguration.PACKAGE_NAME_LIFECYCLELISTENER);
    }
}
