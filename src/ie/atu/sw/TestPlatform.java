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
 * This class provides various methods for model evaluation
 */
public abstract class TestPlatform {
	
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
	
	public double[][] crossValidate(int k, int seed, boolean verbose) throws Exception {
		return crossValidate(trainX, trainExpected, k, seed, network, verbose);
	}
	
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

	public TestPlatform testModel(ModelType type, boolean verbose) throws Exception {
		Stats stats = new Stats();
		stats.scoreModel(testExpected, getResultSoftMax(testX, network), classNames, type, verbose);
		return this;
	}

	public TestPlatform loadModel(String fileName) throws Exception {
		network = Aicme4jUtils.load("./" + fileName);
		System.out.println(network.toString());
		return this;
	}

	private double[][][] sliceData(double[][] data, int w1, int w2) {
		double[][] trainX = new double[data.length][w1];
		double[][] expectedY = new double[data.length][w2];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(data[i], 0, trainX[i], 0, w1);
			System.arraycopy(data[i], w1, expectedY[i], 0, w2);
		}
		return new double[][][] { trainX, expectedY };
	}

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

	private double[][] getResult(double[][] testDataX, NeuralNetwork network) throws Exception {
		double[][] ret = new double[testDataX.length][];
		for(int i = 0; i < testDataX.length; i++) {
			network.process(testDataX[i], Output.LABEL_INDEX);
			double[] store = network.getOutputLayer();
			ret[i] = Arrays.copyOf(store, store.length);
		}
		return ret;
	}
	
	private double[][] getResultSoftMax(double[][] testDataX, NeuralNetwork network) throws Exception {
		double[][] ret = new double[testDataX.length][];
		for(int i = 0; i < testDataX.length; i++) {
			network.process(testDataX[i], Output.LABEL_INDEX);
			double[] store = new Stats().softMax(network.getOutputLayer());
			ret[i] = Arrays.copyOf(store, store.length);
		}
		return ret;
	}

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
												.save("./resources/gridSearch/grid_model" + String.format("%04d", count)
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
