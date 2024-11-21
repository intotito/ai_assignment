package ie.atu.sw.stat;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * A utility class that provides basic statistical functions.
 */
public class Stats {
	/**
	 * Mean Square Error
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the mean square error
	 */
	public double getMSE(double[][] expected, double[][] prediction) {
		return getSSE(expected, prediction) / expected.length;
	}
	/**
	 * Root Mean Square Error
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the root mean square error
	 */
	public double getRMSE(double[][] expected, double[][] prediction) {
		return Math.sqrt(getMSE(expected, prediction));
	}
	/**
	 * Sum of Square Error
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the sum of square error
	 */
	public double getSSE(double[][] expected, double[][] prediction) {
		return IntStream.range(0, expected.length).mapToDouble(i -> Math.pow(expected[i][0] - prediction[i][0], 2))
				.sum();
	}
	/**
	 * Sum of Square Regression
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the sum of square regression
	 */
	public double getSSR(double[][] expected, double[][] prediction) {
		double mean = getMean(expected);
		return IntStream.range(0, prediction.length).mapToDouble(i -> Math.pow(prediction[i][0] - mean, 2)).sum();
	}
	/**
	 * Mean Absolute Error
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the mean absolute error
	 */
	public double getMAE(double[][] expected, double[][] prediction) {
		return IntStream.range(0, prediction.length).mapToDouble(i -> Math.abs(prediction[i][0] - expected[i][0]))
				.average().getAsDouble();
	}
	/**
	 * Root Squared
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @param verbose true if detailed info will be printed
	 * @return the root squared
	 */
	@Regression(message = "R Squared (R2)")
	public double getRSquared(double[][] expected, double[][] prediction, String[] classNames, boolean verbose) {
		double ssr = getSSR(expected, prediction);
		return ssr / (ssr + getSSE(expected, prediction));
	}
	/**
	 * Adjusted Root Squared
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @param verbose true if detailed info will be printed
	 * @return the adjusted root squared
	 */
	@Regression(message = "Adjusted R Squared (aR2)")
	public double getARSquared(double[][] expected, double[][] prediction, String[] classNames, boolean verbose) {
		return 1 - ((1 - getRSquared(expected, prediction, classNames, false)) * (expected.length - 1)
				/ (expected.length - expected[0].length - 1));
	}
	/**
	 * Root Squared
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the root squared
	 */	
	public double[] softMax(double[] vector) {
		double sum = IntStream.range(0, vector.length).mapToDouble(i -> Math.exp(vector[i])).sum();
		return IntStream.range(0,  vector.length).mapToDouble(i -> Math.exp(vector[i]) / sum).toArray();
	}
	/**
	 * Mean of the values
	 * @param values input array
	 * @param axis index of the multi-dimensional array to compute
	 * @return the mean
	 */
	public double getMean(double[][] values, int axis) {
		return IntStream.range(0, values.length).mapToDouble(i -> values[i][axis]).average().getAsDouble();
	}
	/**
	 * Mean of the values, with implicit assumption that the index of 
	 * the multi-dimensional array to evaluate is 0.
	 * @param values input array
	 * @return the mean
	 */
	public double getMean(double[][] values) {
		return getMean(values, 0);
	}
	/**
	 * Argmax of the value. Returns index of the item with the highest value. 
	 * @param values input array
	 * @return the argmax
	 */
	public int argMax(double[] values) {
		final double max = Arrays.stream(values).max().getAsDouble();
		return IntStream.range(0, values.length).map(i -> (values[i] == max) ? i : 0).sum();
	}
	/**
	 * Computes the confusion matrix.
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @return the confusion matrix with dimension of <code>classes by classes</code>
	 */
	public int[][] getConfusionMatrix(double[][] expected, double[][] prediction){
		int classes = expected[0].length;
		int[][] matrix = new int[classes][classes];
		for (int i = 0; i < expected.length; i++) {
			for (int row = 0; row < expected[i].length; row++) {
				for (int col = 0; col < prediction[i].length; col++) {
					int predicted = argMax(prediction[i]);
					int expect = argMax(expected[i]);
					if (expect == row && predicted == col) {
						matrix[row][col]++;
					}
				}
			}
		}
		return matrix;
	}
	/**
	 * Print the confusion matrix in a pretty format
	 * @param matrix the confusion matrix
	 * @param features the name of the features
	 */
	public void printConfusionMatrix(int[][] matrix, String[] features) {
		int col = 15;
		String sFormat = "%-" + col + "s";
		String iFormat = "%-" + col + "d";
		System.out.printf("\n" + sFormat, "Features");
		for (int i = 0; i < features.length; i++) {
			System.out.printf(sFormat, features[i]);
		}
		for (int i = 0; i < matrix.length; i++) {
			System.out.printf("\n" + sFormat, features[i]);
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.printf(iFormat, matrix[i][j]);
			}
		}
		IntStream.range(0,  col * (matrix[0].length + 1) + matrix[0].length + 1).forEach(i -> System.out.print(i == 0 ? "\n-" : "-"));
	}
	
	/**
	 * Accuracy given F1-Score
	 * @param f1Scores the F1-Score
	 * @return the accuracy
	 */
	public double getAccuracy(double[][] f1Scores) {
		int total = IntStream.range(0, f1Scores.length).map(z -> (int)f1Scores[z][f1Scores[z].length - 1]).sum();
		return  IntStream.range(0, f1Scores.length).
				mapToDouble(z -> f1Scores[z][f1Scores[z].length - 1] * (f1Scores[z][0] + f1Scores[z][1]) / 2.0).sum() / total * 100;
	}
	/**
	 * Accuracy given Confusion Matrix
	 * @param f1Scores the Confusion Matrix
	 * @return the accuracy
	 */
	public double getAccuracy(int[][] matrix) {
		int total = IntStream.range(0,  matrix.length)
				.map(i -> IntStream.range(0,  matrix[i].length).map(z -> matrix[i][z]).sum()).sum();
		return IntStream.range(0, matrix.length).mapToDouble(i -> matrix[i][i]).sum() / total * 100;
	}
	/**
	 * Print Accuracy report
	 * @param f1Scores the F1-Scores
	 */
	public void printAccuracyReport(double[][] f1Scores) {
		int col = 15;
		String sFormat = "%-" + col + "s";
		String iFormat = "%-" + col + "d";
		String fFormat = "%-" + col + ".2f";
		String[] titles = { "Accuracy", "Macro Avg.", "Weighted Avg" };
		int total = IntStream.range(0, f1Scores.length).map(z -> (int)f1Scores[z][f1Scores[z].length - 1]).sum();
		for (int i = 0; i < titles.length; i++) {
			final int I = i;
			System.out.printf("\n" + sFormat, titles[i]);
			for (int j = 0; j < f1Scores.length + 1; j++) {
				final int J = j;
				if (i == 0) {
					if (j == (f1Scores.length - 1)) {
						System.out.printf(fFormat, getAccuracy(f1Scores));
					} else if(j == (f1Scores.length)) {
						System.out.printf(iFormat, total);
					}
					else {
						System.out.printf(sFormat, "");
					}
				} else if(i == 1){
					if (j == (f1Scores.length)) {
						System.out.printf(iFormat, total);
					} else {
						double value = Arrays.stream(f1Scores).map(arr -> arr[J]).mapToDouble(Double::doubleValue).average().getAsDouble();
						System.out.printf(fFormat, value * 100);
					}
				} else if(i == 2) {
					if (j == (f1Scores.length)) {
						System.out.printf(iFormat, total);
					} else {
						double value = Arrays.stream(f1Scores).map(arr -> arr[J] * arr[f1Scores[J].length - 1]).mapToDouble(Double::doubleValue).sum();
						System.out.printf(fFormat, 100 * value / total);
					}
				}
			}
		}
		IntStream.range(0,  col * (f1Scores[0].length) + f1Scores[0].length).forEach(i -> System.out.print(i == 0 ? "\n-" : "-"));
	}
	/**
	 * Print the F1-Scores
	 * @param f1Scores the F1-Scores
	 * @param features the name of the features
	 */
	public void printF1Scores(double[][] f1Scores, String[] features) {
		int col = 15;
		String sFormat = "%-" + col + "s";
		String iFormat = "%-" + col + "d";
		String fFormat = "%-" + col + ".2f";
		String[] metrics = { "Precision(%)", "Recall(%)", "F1-Score(%)", "Support" };
		System.out.printf("\n" + sFormat, "");
		for (int i = 0; i < metrics.length; i++) {
			System.out.printf(sFormat, metrics[i]);
		}
		for (int i = 0; i < f1Scores.length; i++) { // order by row
			System.out.printf("\n" + sFormat, features[i]);
			for (int j = 0; j < metrics.length; j++) {
				if(j == metrics.length - 1) {
					System.out.printf(iFormat, (int)f1Scores[i][j]);
				} else {
					System.out.printf(fFormat, f1Scores[i][j] * 100);
				}
			}
		}
		IntStream.range(0,  col * (f1Scores[0].length) + f1Scores[0].length).forEach(i -> System.out.print(i == 0 ? "\n-" : "-"));
	}
	/**
	 * Get F1-Score given Confusion Matrix
	 * @param matrix the Confusion Matrix
	 * @return the F1-Scores
	 */
	public double[][] getF1Scores(int[][] matrix) {
		String[] metrics = { "Precision(%)", "Recall(%)", "F1-Score(%)", "Support" };
		double[][] scores = new double[matrix.length][metrics.length];
		for (int i = 0; i < matrix.length; i++) { // order by row
			final int I = i;
			double sumRow = Arrays.stream(matrix[i]).sum();
			double sumCol = IntStream.range(0, matrix[i].length).mapToDouble(z -> matrix[z][I]).sum();
			double precision = 0, recall = 0, f1;
			for (int j = 0; j < metrics.length; j++) {
				if (j == 0) { // Precision
					precision = matrix[i][i] / (sumCol == 0 ? 1 : sumCol);
					scores[i][j] = precision;
				} else if (j == 1) {
					recall = matrix[i][i] / (sumRow == 0 ? 1 : sumRow);
					scores[i][j] = recall;
				} else if (j == 2) {
					f1 = 2 * (precision * recall) / ((precision + recall) == 0 ? 1 : (precision + recall));
					scores[i][j] = f1;
				} else {
					scores[i][j] = (int) sumRow;
				}
			}
		}
		return scores;
	}
	/**
	 * Get the Accuracy of a model					   
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @param verbose true if detailed info will be printed
	 * @return the accuracy
	 */
	@Classification(message = "Model Accuracy:")
	public double modelAccuracy(double[][] expected, double[][] prediction, String[] classNames, boolean verbose) {
		int[][] matrix = getConfusionMatrix(expected, prediction);
		double[][] f1Scores = getF1Scores(matrix);
		if(verbose) {
			printConfusionMatrix(matrix, classNames);
			printF1Scores(f1Scores, classNames);
			printAccuracyReport(f1Scores);
		}
		return getAccuracy(f1Scores);
	}
	/**
	 * The Mean Category Cross-Entropy
	 * @param expected the truth value
	 * @param prediction the predicted value
	 * @param verbose true if detailed will be printed
	 * @return the mean category cross-entropy
	 */
	@Classification(message = "Mean Category Cross-Entropy")
	public double getMCCE(double[][] expected, double[][] prediction, String[] classNames, boolean verbose) {
		double sum = 0;
		for(int i = 0; i < expected.length; i++) {
			for(int j = 0; j < expected[i].length; j++) {
				sum += -1 * (expected[i][j] * Math.log(prediction[i][j] + Math.pow(10,  -100)) / expected[i].length);
			}
		}
		return sum / expected.length;
	}

	/**
	 * Evaluates the performance of a NeuralNetwork using annotated metrics. This method calls all relevant 
	 * methods required to evaluate a model reflectively.
	 * @param expected the expected true values
	 * @param prediction the predicted value
	 * @param classNames names of the dependent variables
	 * @param type the type of model
	 * @param verbose true if detailed will be printed
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void scoreModel(double[][] expected, double[][] prediction, String[] classNames, ModelType type, boolean verbose)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends Annotation> ann = switch (type) {
		case REGRESSION -> Regression.class;
		case CLASSIFICATION -> Classification.class;
		};
		Class<?> clazz = getClass();
		Map<String, Double> values = new HashMap<String, Double>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(ann)) {
				var myAnn = method.getDeclaredAnnotation(ann);
				Method mMethod = ann.getDeclaredMethod("message");
				method.setAccessible(true);
				String message = (String) mMethod.invoke(myAnn);
				double result = (double)method.invoke(this, expected, prediction, classNames, verbose);
				values.put(message, result);
			}
		}
		for(var kv : values.entrySet()) {
			System.out.printf("\n%-30s\t%.4f", kv.getKey(), kv.getValue());
		}
	}
}
