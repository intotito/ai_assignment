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

import jhealy.aicme4j.net.Aicme4jUtils;
import jhealy.aicme4j.net.NeuralNetwork;
import jhealy.aicme4j.net.Output;

public abstract class TestPlatform {
	protected NeuralNetwork network;
	protected double[][] trainX, trainExpected;
	double[][] testX, testExpected;
	protected int seed;

	public abstract TestPlatform train(String fileName) throws Exception;

	protected double[][][] splitData(double[][] data, double fraction, int seed) {
		List<double[]> dataSet = new ArrayList<double[]>(Arrays.asList(data));
		Collections.shuffle(dataSet, new Random(seed));
		List<double[]> trainSet = new ArrayList<>();
		for (int i = 0; i < data.length * fraction; i++) {
			trainSet.add(dataSet.remove(ThreadLocalRandom.current().nextInt(0, dataSet.size())));
		}
		return new double[][][] { trainSet.toArray(double[][]::new), dataSet.toArray(double[][]::new) };
	}

	protected TestPlatform loadData(String fileName, int w1, int w2, double divRatio) throws IOException {
		double[][][] data = splitData(getData(fileName, w1 + w2), divRatio, seed);
		double[][][] trainData = sliceData(data[0], w1, w2);
		double[][][] testData = sliceData(data[1], w1, w2);
		trainX = trainData[0];
		trainExpected = trainData[1];
		testX = testData[0];
		testExpected = testData[1];
		return this;
	}

	protected TestPlatform testTrain(boolean verbose) throws Exception {
		testPrediction(trainX, trainExpected, network, verbose);
		return this;
	}

	protected TestPlatform testTest(boolean verbose) throws Exception {
		testPrediction(testX, testExpected, network, verbose);
		return this;
	}

	public TestPlatform loadModel(String fileName) throws Exception {
		network = Aicme4jUtils.load("./" + fileName);
		return this;
	}

	protected double[][][] sliceData(double[][] data, int w1, int w2) {
		double[][] trainX = new double[data.length][w1];
		double[][] expectedY = new double[data.length][w2];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(data[i], 0, trainX[i], 0, w1);
			System.arraycopy(data[i], w1, expectedY[i], 0, w2);
		}
		return new double[][][] { trainX, expectedY };
	}

	protected double[][] getData(String fileName, int width) throws IOException {
		File file = new File("./" + fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		double[][] dataset = new double[(int) reader.lines().count() - 1][15];
		reader.close();
		try (Stream<String> lines = new BufferedReader(new InputStreamReader(new FileInputStream(file))).lines()) {
			int[] i = { 0 };
			lines.skip(1).forEach(line -> {
				String[] values = line.split("\\s*,\\s*");

				double[] dd = Arrays.stream(values).map(Double::valueOf).mapToDouble(Double::doubleValue).toArray();// toArray(Double[]::new);
				dataset[i[0]++] = dd;

			});
		}
		return dataset;
	}

	public static void normalizeData(String[] columnNames, double[][] data, double[][] expected, String fileName)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(Arrays.stream(columnNames).collect(Collectors.joining(",")) + "\n");
//		sb.append("f1, f2, f3, f4, f5, f6, f7, cp, im, pp, imU, om, omL, imL, imS\n");
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

	protected double getMSE(double[] expected, double[] prediction) {
		return IntStream.range(0, expected.length).mapToDouble(i -> Math.pow(expected[i] - prediction[i], 2)).sum()
				/ expected.length;
	}

	protected double getRMSE(double[] expected, double[] prediction) {
		double mean = getMean(expected);
		double sum = 0;
		for (int i = 0; i < expected.length; i++) {
			sum += Math.pow(prediction[i] - mean, 2);
		}
		double error = sum / expected.length;
		return Math.sqrt(error);
	}

	protected double getSEE(double[] expected, double[] prediction) {
		double sum = 0;
		for (int i = 0; i < expected.length; i++) {
			sum += Math.pow(prediction[i] - expected[i], 2);
		}
		double error = sum / (expected.length - 2);
		return Math.sqrt(error);
	}

	protected double getMean(double[] values) {
		return Arrays.stream(values).average().getAsDouble();
	}
	
	protected void testPrediction(double[][] testDataX, double[][] testExpectedY, NeuralNetwork network, boolean verbose) throws Exception {
		String line = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -";
		if(verbose)
		System.out.print("Feature\t\t|");
		for(int i = 0; i < testDataX[0].length && verbose; i++) {
			System.out.printf("\t%s%d", "F", i + 1);
		}
		System.out.println();
		double sumAccuracy = 0;
		double sseAccuracy = 0;
		double passMark = 0;
		for(int i = 0; i < testDataX.length; i++) {
			if(verbose)
			System.out.printf("Test Data\t|\t");
			for(int j = 0; j < testDataX[i].length && verbose; j++) {
				System.out.printf("%.3f,\t", testDataX[i][j]);
			}
			if(verbose)
			System.out.printf("\nExpected\t|\t");
			for(int k = 0; k < testExpectedY[i].length && verbose; k++) {
				System.out.printf("%.3f,\t", testExpectedY[i][k]);
			}
			int predIndex = (int)network.process(testDataX[i], Output.LABEL_INDEX);
			double[] pred = network.getOutputLayer();
			if(verbose)
			System.out.printf("\nPredicted\t|\t");
			for(int z = 0; z < pred.length && verbose; z++) {
				System.out.printf("%.3f,\t", pred[z]);
			}
//			double accuracy = (1 - getMSE(testExpectedY[i], pred)) * 100;
			double accuracy = getMSE(testExpectedY[i], pred);

			sumAccuracy += accuracy;
			double sAccuracy = ((1 - getSEE(testExpectedY[i], pred)) * 100);
			sseAccuracy += sAccuracy;
			boolean pass = Aicme4jUtils.getMaxIndex(testExpectedY[i]) == predIndex;//match(testExpectedY[i], pred);
			passMark += pass ? 1 : 0;
			if(verbose)
			System.out.printf("\n%s\t\tMSE: %.2f%s\tSSE: %.2f%s \t %s\n", line, accuracy , "%", sAccuracy, "%", pass ? "[OK]" : "[ERROR]");
		}
		System.out.printf("MeanSquaredError: %.2f%s\t StandardErrorEstimate: %.2f%s \t PredictionAccuracy: %.2f%s\n", 
				sumAccuracy / testDataX.length, "%", sseAccuracy / testDataX.length, "%", passMark * 100 / testDataX.length, "%");
	}
}
