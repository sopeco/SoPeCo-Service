package org.sopeco.service.test.rest.fake;

import java.security.Provider.Service;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.NormalizedRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopeco.engine.measurementenvironment.AbstractMEController;
import org.sopeco.engine.measurementenvironment.InputParameter;
import org.sopeco.engine.measurementenvironment.ObservationParameter;
import org.sopeco.engine.measurementenvironment.app.MECApplication;
import org.sopeco.engine.measurementenvironment.socket.SocketAcception;
import org.sopeco.persistence.dataset.ParameterValueList;
import org.sopeco.persistence.entities.definition.ExperimentTerminationCondition;
import org.sopeco.persistence.entities.exceptions.ExperimentFailedException;
import org.sopeco.service.configuration.ServiceConfiguration;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * MatrixMultiplicationMEC is an example for a Measurement Environment
 * Controller (MEController). Each MEController has to extend the
 * AbstractMEController class which is contained in the
 * org.sopeco.core-VERSION-jar-with-dependencies.jar.
 * 
 * In this example we use the MatrixMultiplicationMEC MEController to
 * investigate the relationship between the response time of a matrix
 * multiplication operation (of different mathematical libraries) and the
 * corresponding matrix sizes.
 * 
 * @author Alexander Wert, Christoph Heger
 * 
 */
public class MatrixMultiplicationMEC extends AbstractMEController {
	/**
	 * Logger used for debugging and log-information.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MatrixMultiplicationMEC.class);

	/**
	 * String constant for the COLT math library library
	 */
	private static final String COLT = "colt";

	/**
	 * String constant for the COMMONS math library library
	 */
	private static final String COMMONS = "commons";

	/**
	 * String constant for the measurement environment controller name
	 */
	private static final String MEC_NAME = "MatrixDemo";

	/**
	 * String constant for the measurement environment controller identifier
	 */
	private static final String MEC_ID = "MatrixMultiplicationMECId";

	/**
	 * Random number generator for rawMatrix creation
	 */
	private NormalizedRandomGenerator generator;

	/**
	 * matrixWidth is an integer input parameter defining the width of the
	 * matrix to be multiplied. Input parameter have to be marked with the @InputParameter
	 * annotation!
	 */
	@InputParameter(namespace = "my.input")
	int matrixWidth = 1;

	/**
	 * matrixWidth is an integer input parameter defining the height of the
	 * matrix to be multiplied. Input parameter have to be marked with the @InputParameter
	 * annotation!
	 */
	@InputParameter(namespace = "my.input")
	int matrixHeight = 1;

	/**
	 * library is a String input parameter describing which math library should
	 * be used for multiplication. Input parameter have to be marked with the @InputParameter
	 * annotation!
	 */
	@InputParameter(namespace = "my.input")
	String library = "";

	/**
	 * randomGeneratorSeed is a long parameter setting the seed used by the
	 * random generator for initialization. Input parameter have to be marked
	 * with the @InputParameter annotation!
	 */
	@InputParameter(namespace = "my.input")
	int randomGeneratorSeed = 111728;

	/**
	 * The sole observation parameter is the response time. Each observation
	 * parameter must be of the type ParameterValueList<T>, whereby T describes
	 * the actual type of the observation parameter. In this case it is a Double
	 * parameter. Observation parameter have to be marked with the @ObservationParameter
	 * annotation!
	 */
	@ObservationParameter(namespace = "my.output")
	ParameterValueList<Double> responseTime;

	/**
	 * Constructor. Here, we define which termination conditions for an
	 * experiment are supported by this MEController. If required, you can
	 * specify your own termination conditions.
	 */
	public MatrixMultiplicationMEC() {
		addSupportedTerminationConditions(ExperimentTerminationCondition
				.createNumberOfRepetitionsTC());
	}

	/**
	 * This functions is called to define which observation parameters should be
	 * included into the result set.
	 */
	@Override
	protected void defineResultSet() {
		addParameterObservationsToResult(responseTime);
	}

	/**
	 * This method is called when a experiment series is finished. In
	 * particular, this method can be used to do clean-up jobs after the
	 * execution of an experiment series. In this example we do not have to
	 * clean up anything, and thus, will not implement this method.
	 */
	@Override
	protected void finalizeExperimentSeries() {
		LOGGER.info("Finalizing experiment series");
	}

	/**
	 * This method is called to initialize the MEController. In this example, we
	 * initialization a random number generator to fill rawMatrix.
	 */
	@Override
	protected void initialize() {
		LOGGER.info("Initializing experiment series");
		this.generator = createRandomGenerator();
	}

	/**
	 * This method is called to prepare an experiment series. In this example,
	 * we do not have any preparation tasks for the experiment series.
	 */
	@Override
	protected void prepareExperimentSeries() {
		LOGGER.info("Preparing experiment series - nothing todo");
	}

	/**
	 * Executes a single experiment run. The values of all parameters annotated
	 * with @InputParameter are set automatically, such that the parameters can
	 * be used directly in this method.
	 * 
	 * In this example, we first create a matrix for the multiplication which is
	 * stored in a double array (rawMatrix). Depending on the value of the
	 * library input parameter, one of the multiplication methods is called
	 * (each representing a different math library).
	 */
	@Override
	protected void runExperiment() throws ExperimentFailedException {
		LOGGER.info("Starting experiment run");
		double[][] rawMatrix = createRandomRawMatrix(matrixWidth, matrixHeight);

		if (library.equals(COMMONS)) {
			commonsMathMatrixMultiply(rawMatrix);
		} else if (library.equals(COLT)) {
			coltMatrixMutliply(rawMatrix);
		}

		LOGGER.info("Finished experiment run");

	}

	/**
	 * COMMONS multiplication library. Multiply the passed rawMatrix with its
	 * transpose N times (where N is the number of repetitions, defined by the
	 * termination condition) and measure the response time of the
	 * multiplication operation. The ObservationParameters do not have to be
	 * initialized. This is done in the abstract class. Instead, all observation
	 * parameters can be used directly by calling the addValue() method to add
	 * new observation values.
	 * 
	 * @param rawMatrix
	 *            matrix to be multiplied with its transpose
	 */
	private void commonsMathMatrixMultiply(double[][] rawMatrix) {

		RealMatrix leftMatrix = MatrixUtils.createRealMatrix(rawMatrix);
		RealMatrix rightMatrix = leftMatrix.transpose();

		for (int i = 0; i < getNumberOfRepetitions(); i++) {
			double start = System.currentTimeMillis();
			leftMatrix.multiply(rightMatrix);
			double end = System.currentTimeMillis();
			responseTime.addValue(new Double(end - start));
		}
	}

	/**
	 * COLT multiplication library. Multiply the passed rawMatrix with its
	 * transpose N times (where N is the number of repetitions, defined by the
	 * termination condition) and measure the response time of the
	 * multiplication operation. The ObservationParameters do not have to be
	 * initialized. This is done in the abstract class. Instead, all observation
	 * parameters can be used directly by calling the addValue() method to add
	 * new observation values.
	 * 
	 * @param rawMatrix
	 *            matrix to be multiplied with its transpose
	 */
	private void coltMatrixMutliply(double[][] rawMatrix) {

		DenseDoubleMatrix2D leftMatrix = new DenseDoubleMatrix2D(rawMatrix);
		DenseDoubleMatrix2D rightMatrix = (DenseDoubleMatrix2D) leftMatrix
				.viewDice();

		Algebra alg = new Algebra();

		for (int i = 0; i < getNumberOfRepetitions(); i++) {
			double start = System.currentTimeMillis();
			alg.mult(leftMatrix, rightMatrix);
			double end = System.currentTimeMillis();
			responseTime.addValue(new Double(end - start));
		}
	}

	/**
	 * Create a random matrix with the specified size.
	 * 
	 * @param matrixWidth
	 *            the width of the matrix
	 * @param matrixHeight
	 *            the height of the matrix
	 * @return a generated matrix as double array representation
	 */
	private double[][] createRandomRawMatrix(int matrixWidth, int matrixHeight) {

		LOGGER.info("Create matrix {} x {}", matrixWidth, matrixHeight);

		double[][] rawData = new double[matrixHeight][matrixWidth];

		for (int i = 0; i < matrixHeight; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				rawData[i][j] = generator.nextNormalizedDouble();
			}
		}

		return rawData;
	}

	/**
	 * Create a random generator for random numbers following a normal
	 * distribution.
	 * 
	 * @return a random number generator
	 */
	private NormalizedRandomGenerator createRandomGenerator() {

		RandomGenerator rg = new JDKRandomGenerator();
		rg.setSeed(randomGeneratorSeed);

		GaussianRandomGenerator generator = new GaussianRandomGenerator(rg);

		return generator;
	}

	public static void start() {
	
		SocketAcception.open(8089);
		try {
			Thread.sleep(4000);
			System.out.println("weiter");
		} catch (Exception e) {
		
		}
		
		LOGGER.debug("+++++++++++++++++++++++");
		
		MECApplication mecapp = MECApplication.get();
		mecapp.addMeasurementController("ABC", new MatrixMultiplicationMEC());
		mecapp.addMeasurementController("Testcontroller", new MatrixMultiplicationMEC());
		mecapp.addMeasurementController("DasGleicheNochmal", new MatrixMultiplicationMEC());
		mecapp.socketConnect(ServiceConfiguration.MEC_SOCKET_HOST, ServiceConfiguration.MEC_SOCKET_PORT, MEC_ID);
	
		LOGGER.debug("+++++++++++++++++++++++");
	}

}
