package ie.atu.sw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ie.atu.sw.stat.ModelType;
import ie.atu.sw.stat.Stats;
import jhealy.aicme4j.NetworkBuilderFactory;
import jhealy.aicme4j.net.Activation;
import jhealy.aicme4j.net.Aicme4jUtils;
import jhealy.aicme4j.net.Loss;
import jhealy.aicme4j.net.NeuralNetwork;
import jhealy.aicme4j.net.Output;
/**
 * This class provides various methods for data processing
 */
public abstract class TestPlatform {
	public static final String GRID_PATH = "./resources/gridSearch/grid_model";
	public static final String TRAINING_DATA = "./resources/data/train_data.csv";
	/**
	 * This class is used to store model performance metrics in Grid Search
	 */
	protected record ModelInfo(String networkInfo, long time, double[][] scores) implements Comparable<ModelInfo> {
		public int compareTo(ModelInfo a) {
			Stats stats = new Stats();
			return (int) (stats.getMean(a.scores(), 0) - stats.getMean(scores(), 0));
		}
	}
	
	protected NeuralNetwork network;
	protected double[][] trainX, trainExpected;
	protected double[][] testX, testExpected;
	protected int seed;
	private String[] classNames;

	public abstract TestPlatform train(String fileName) throws Exception;
	
	/**
	 * K-Fold Cross-Validation
	 * @param k the number of folds
	 * @param seed the random seed
	 * @param verbose true if detailed info will be displayed
	 * @return k-length two dimensional array of results
	 */
	public double[][] crossValidate(int k, int seed, boolean verbose) throws Exception {
		return crossValidate(trainX, trainExpected, k, seed, network, verbose);
	}
	/**
	 * K-Fold Cross-Validation
	 * @param X training data
	 * @param y truth values
	 * @param ann Neural Network under evaluation
	 * @param k the number of folds
	 * @param seed the random seed
	 * @param verbose true if detailed info will be displayed
	 * @return k-length two dimensional array of results
	 */
	public double[][] crossValidate(double[][] X, double[][] y, int k, int seed, NeuralNetwork ann, boolean verbose) throws  Exception {
		Stats stats = new Stats();
		Random random = new Random(seed);
		Integer[] indices = IntStream.range(0,  X.length).boxed().toArray(Integer[]::new);
		List<Integer> in = Arrays.asList(indices);
		Collections.shuffle(in, random);
		indices = in.stream().mapToInt(Integer::intValue).boxed().toArray(Integer[]::new);
		double[][] scores = new double[k][2];
		for(int i = 0; i < k; i++) {
			int count = indices.length / k;
			double[][] trainXIJ = new double[count * (k - 1)][];
			double[][] trainExpectedIJ = new double[count * (k - 1)][];

			double[][] validXIJ = new double[count][];
			double[][] validExpectedIJ = new double[count][];
			for(int j = 0, vC = 0, tC = 0; j < (indices.length / k) * k; j++) {
				if(j >= i * count && j < (i + 1) * count) {
					validXIJ[vC] = X[indices[j]];
					validExpectedIJ[vC] = y[indices[j]];
					vC++;
				} else {
					trainXIJ[tC] = X[indices[j]];
					trainExpectedIJ[tC] = y[indices[j]];
					tC++;
				}
			}
			int[][] matrix = stats.getConfusionMatrix(validExpectedIJ, getResult(validXIJ, ann));
			double accuracy = stats.getAccuracy(matrix);
			scores[i] = new double[]{accuracy, stats.getMCCE(validExpectedIJ, getResultSoftMax(validXIJ, ann), classNames, false)};
			if(verbose) {
				stats.printConfusionMatrix(matrix, classNames);
				stats.printF1Scores(stats.getF1Scores(matrix), classNames);
				stats.printAccuracyReport(stats.getF1Scores(matrix));
				System.out.println();
			}
			
		}
		return scores;
	}

	/**
	 * Split training data into training and test sets
	 * @param data training data to be split
	 * @param fraction fraction of data to be reserved for testing
	 * @param seed the random seed
	 * @return a three dimensional array consisting of training and test data
	 */
	private double[][][] splitData(double[][] data, double fraction, int seed) {
		List<double[]> dataSet = new ArrayList<double[]>(Arrays.asList(data));
		Random r = new Random(seed);
		Collections.shuffle(dataSet, r);
		List<double[]> trainSet = new ArrayList<>();
		for (int i = 0; i < data.length * fraction; i++) {
			trainSet.add(dataSet.remove(ThreadLocalRandom.current().nextInt(0, dataSet.size())));
		}
		return new double[][][] { trainSet.toArray(double[][]::new), dataSet.toArray(double[][]::new) };
	}

	/**
	 * Load data from file and split into training and test sets
	 * @param fileName the name of the data file
	 * @param w1 number of independent variables
	 * @param w2 number of dependent variables
	 * @param divRatio fraction of data to be reserved for testing
	 * @return self
	 * @throws IOException
	 */
	public TestPlatform loadData(String fileName, int w1, int w2, double divRatio) throws IOException {
		double[][][] data = splitData(getData(fileName, w1, w2), divRatio, seed);
		double[][][] trainData = sliceData(data[0], w1, w2);
		double[][][] testData = sliceData(data[1], w1, w2);
		trainX = trainData[0];
		trainExpected = trainData[1];
		testX = testData[0];
		testExpected = testData[1];
		return this;
	}
	/**
	 * Evaluate the model
	 * @param type the type of model evaluation to be run
	 * @param verbose true if detailed info will be displayed
	 * @return self
	 * @throws Exception
	 */
	public TestPlatform testModel(ModelType type, boolean verbose) throws Exception {
		Stats stats = new Stats();
		stats.scoreModel(testExpected, getResultSoftMax(testX, network), classNames, type, verbose);
		return this;
	}
	/**
	 * Load a trained model from file
	 * @param fileName the name of the model file
	 * @return self
	 * @throws Exception
	 */
	public TestPlatform loadModel(String fileName) throws Exception {
		network = Aicme4jUtils.load("./" + fileName);
		System.out.println(network.toString());
		return this;
	}
	/**
	 * Split data into training and test sets
	 * @param data the data to be split
	 * @param w1 number of independent variables
	 * @param w2 number of dependent variables
	 * @return three dimensional array containing the training and test dataset
	 */
	private double[][][] sliceData(double[][] data, int w1, int w2) {
		double[][] trainX = new double[data.length][w1];
		double[][] expectedY = new double[data.length][w2];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(data[i], 0, trainX[i], 0, w1);
			System.arraycopy(data[i], w1, expectedY[i], 0, w2);
		}
		return new double[][][] { trainX, expectedY };
	}
	/**
	 * Load data from file
	 * @param fileName the name of the file
	 * @param w1 number of independent variables
	 * @param w2 number of dependent variables
	 * @return data from file as two dimensional array
	 * @throws IOException
	 */
	private double[][] getData(String fileName, int w1, int w2) throws IOException {
		File file = new File("./" + fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		double[][] dataset = new double[(int) reader.lines().count() - 1][];
		reader.close();
		try (Stream<String> lines = new BufferedReader(new InputStreamReader(new FileInputStream(file))).lines()) {
			int[] i = { -1 };
			lines
//			.skip(1)
			.forEach(line -> {
				String[] values = line.split("\\s*,\\s*");
				if(i[0] == -1) {
					classNames = Arrays.stream(values).skip(w1).toArray(String[]::new);
					i[0]++;
				} else {
					double[] dd = Arrays.stream(values).map(Double::valueOf).mapToDouble(Double::doubleValue).toArray();// toArray(Double[]::new);
					dataset[i[0]++] = dd;
				}
			});
		}
		return dataset;
	}
	/**
	 * Write data to file
	 * @param columnNames the names of the features
	 * @param data independent variables
	 * @param expected dependent variables
	 * @param fileName name of the file
	 * @throws IOException
	 */
	public static void arrayToCSV(String[] columnNames, double[][] data, double[][] expected, String fileName)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(Arrays.stream(columnNames).collect(Collectors.joining(",")) + "\n");
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				sb.append(data[i][j] + ", ");
			}
			for (int k = 0; k < expected[i].length; k++) {
				sb.append(expected[i][k]);
				sb.append(k == expected[i].length - 1 ? "\n" : ",");
			}
		}

		String str = sb.toString();
		Path path = Paths.get("./" + fileName);
		byte[] strToBytes = str.getBytes();
		Files.write(path, strToBytes);
	}
	/**
	 * Evaluate input data with a NeuralNetwork
	 * @param testDataX input data to be evaluated
	 * @param network the NeuralNetwork
	 * @return prediction from the NeuralNetwork
	 * @throws Exception
	 */
	private double[][] getResult(double[][] testDataX, NeuralNetwork network) throws Exception {
		double[][] ret = new double[testDataX.length][];
		for(int i = 0; i < testDataX.length; i++) {
			network.process(testDataX[i], Output.LABEL_INDEX);
			double[] store = network.getOutputLayer();
			ret[i] = Arrays.copyOf(store, store.length);
		}
		return ret;
	}
	
	/**
	 * Evaluate the softmax for input data with a NeuralNetwork
	 * @param testDataX input data to be evaluated
	 * @param network the NeuralNetwork
	 * @return softmax prediction from the NeuralNetwork
	 * @throws Exception
	 */
	private double[][] getResultSoftMax(double[][] testDataX, NeuralNetwork network) throws Exception {
		double[][] ret = new double[testDataX.length][];
		for(int i = 0; i < testDataX.length; i++) {
			network.process(testDataX[i], Output.LABEL_INDEX);
			double[] store = new Stats().softMax(network.getOutputLayer());
			ret[i] = Arrays.copyOf(store, store.length);
		}
		return ret;
	}

	/**
	 * Performs a search through the ranges of parameters supplied
	 * @param testDataX training input data
	 * @param expectedY truth data
	 * @param kValid K-Fold validation number
	 * @param hiddenLayers hidden layers to search through
	 * @param hiddenNeurons hidden neuron numbers to search through
	 * @param actFunc activation functions to search through
	 * @param lossFunc loss functions to search through
	 * @param alphas alphas to search through
	 * @param betas betas to search through
	 * @param minErros minimum errors to search through
	 * @param epochs epochs to search through
	 * @return performance information for every model trialed
	 * @throws Exception
	 */
	public ModelInfo[] gridSearch(double[][] testDataX, double[][] expectedY, int kValid, int[] hiddenLayers, int[][] hiddenNeurons,
			Activation[] actFunc, Loss[] lossFunc, double[] alphas, double[] betas, double[] minErros, int[] epochs)
			throws Exception {
		long totalTime = System.currentTimeMillis();
		System.out.println("Grid Search Started....");
		int modelCount = hiddenLayers.length * actFunc.length * lossFunc.length * alphas.length * betas.length
				* minErros.length * epochs.length * hiddenNeurons.length * 2;
		ModelInfo[] mInfo = new ModelInfo[modelCount];
		int count = 0;
		for (int i = 0; i < hiddenLayers.length; i++) {
			for (int j = 0; j < actFunc.length; j++) {
				for (int k = 0; k < lossFunc.length; k++) {
					for (int m = 0; m < alphas.length; m++) {
						for (int n = 0; n < betas.length; n++) {
							for (int p = 0; p < minErros.length; p++) {
								for (int q = 0; q < epochs.length; q++) {
									for (int b = 0; b < (hiddenNeurons[i].length - hiddenLayers[i] + 1); b++) {
										StringBuilder inf = new StringBuilder();
										long time = System.currentTimeMillis();
										var builder = NetworkBuilderFactory.getInstance().newNetworkBuilder();
										builder.inputLayer("Input", testDataX[0].length);
										inf.append(testDataX[0].length).append("-");
										for (int ix = b; ix < hiddenLayers[i] + b; ix++) {
											builder.hiddenLayer("Hidden" + (ix + 1), actFunc[j], hiddenNeurons[i][ix]);

											inf.append(hiddenNeurons[i][ix]).append("-");
										}
										inf.append(expectedY[0].length).append(",").append(actFunc[j].toString())
												.append(",").append(lossFunc[k].toString()).append(",")
												.append(epochs[q]).append(",").append(alphas[m]).append(",")
												.append(betas[n]);
										var network = builder.outputLayer("Output", actFunc[j], expectedY[0].length)
												.outputLayer("Output", actFunc[j], expectedY[0].length)
												.train(testDataX, expectedY, alphas[m], betas[n], epochs[q],
														minErros[p], lossFunc[k])
												.save(GRID_PATH + String.format("%04d", count)
														+ ".ann")
												.build();
										time = System.currentTimeMillis() - time;
										double[][] score = crossValidate(testDataX, expectedY, kValid, seed, network, false);
										mInfo[count++] = new ModelInfo(inf.toString(), time, score);
										System.out.println(Arrays.toString(score[1]));
									}
								}
							}
						}
					}
				}
			}

		}
		System.out
				.println("Grid Search Concluded in " + ((System.currentTimeMillis() - totalTime) / 1000) + " seconds");
		return mInfo;
	}

}
