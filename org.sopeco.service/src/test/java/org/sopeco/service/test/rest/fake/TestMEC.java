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
