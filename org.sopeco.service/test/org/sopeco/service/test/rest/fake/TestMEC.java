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
package org.sopeco.service.test.rest.fake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.AbstractMEController;
import org.sopeco.engine.measurementenvironment.InputParameter;
import org.sopeco.engine.measurementenvironment.app.MECApplication;
import org.sopeco.persistence.entities.exceptions.ExperimentFailedException;
import org.sopeco.service.configuration.ServiceConfiguration;

/**
 * This class is used in the test environment, when the {@link MeasurementControllerService}
 * is tested in {@link MeasurementControllerServiceTest}.
 * 
 * @author Peter Merkert
 */
public class TestMEC extends AbstractMEController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestMEC.class);
	
	/**
	 * The MEC_ID represents the ID, with witchever the MEC can be requested on
	 * the RESTful service.
	 */
	public static final String MEC_ID = TestMEC.class.getName();
	
	public static final String MEC_SUB_ID_1 = "ABC";
	public static final String MEC_SUB_ID_2 = "Helmholtz";
	public static final String MEC_SUB_ID_3 = "Test";
	
	@InputParameter(namespace = "test")
	int abc = 0;

	@Override
	protected void defineResultSet() {
	}

	@Override
	protected void initialize() {
	}

	@Override
	protected void prepareExperimentSeries() {
	}

	@Override
	protected void runExperiment() throws ExperimentFailedException {
		System.out.println(1);
		
		for (int i = 0; i < abc; i++) {
			System.out.println(2);

			try {

				print("started");

				Thread.sleep(2500);
				System.out.println(3);

				print("1");
				print("2");

				Thread.sleep(2500);
				System.out.println(4);

				print("ending");

			} catch (Exception e) {
			}

		}
		System.out.println(5);
	}

	@Override
	protected void finalizeExperimentSeries() {
	}

	/**
	 * Starts the TestMEC 3 times with the names "ABC", "Testcontroller"
	 * and "DasGleicheNochmal".
	 * <br />
	 * The socket connection is afterwards established.
	 * <br />
	 * Before starting: A ServerSocket must listen on the <code>ServiceConfiguration.MEC_SOCKET_HOST</code>
	 * and <code>ServiceConfiguration.MEC_SOCKET_PORT</code>.
	 * <br />
	 * The TestMEC is registered with the class name ("TestMEC") as identifier.
	 */
	public static void start() {
		LOGGER.debug("Connect the TestMEC controller to the RESTful service ServerSocket.");
		
		MECApplication mecapp = MECApplication.get();
		mecapp.addMeasurementController(MEC_SUB_ID_1, new TestMEC());
		mecapp.addMeasurementController(MEC_SUB_ID_2, new TestMEC());
		mecapp.addMeasurementController(MEC_SUB_ID_3, new TestMEC());
		mecapp.socketConnect(ServiceConfiguration.MEC_SOCKET_HOST, ServiceConfiguration.MEC_SOCKET_PORT, MEC_ID);
	}

}
