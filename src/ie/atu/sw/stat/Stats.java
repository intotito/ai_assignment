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

public class Stats {
	public double getMSE(double[][] expected, double[][] prediction) {
		return getSSE(expected, prediction) / expected.length;
	}

	public double getRMSE(double[][] expected, double[][] prediction) {
		return Math.sqrt(getMSE(expected, prediction));
	}

	public double getSSE(double[][] expected, double[][] prediction) {
		return IntStream.range(0, expected.length).mapToDouble(i -> Math.pow(expected[i][0] - prediction[i][0], 2))
				.sum();
	}

	public double getSSR(double[][] expected, double[][] prediction) {
		double mean = getMean(expected);
		return IntStream.range(0, prediction.length).mapToDouble(i -> Math.pow(prediction[i][0] - mean, 2)).sum();
	}

	public double getMAE(double[][] expected, double[][] prediction) {
		return IntStream.range(0, prediction.length).mapToDouble(i -> Math.abs(prediction[i][0] - expected[i][0]))
				.average().getAsDouble();
	}
	
	@Regression(message = "R Squared (R2)")
	public double getRSquared(double[][] expected, double[][] prediction) {
		double ssr = getSSR(expected, prediction);
		return ssr / (ssr + getSSE(expected, prediction));
	}

	@Regression(message = "Adjusted R Squared (aR2)")
	public double getARSquared(double[][] expected, double[][] prediction) {
		return 1 - ((1 - getRSquared(expected, prediction)) * (expected.length - 1)
				/ (expected.length - expected[0].length - 1));
	}
	
	public double[] softMax(double[] vector) {
		double sum = IntStream.range(0, vector.length).mapToDouble(i -> Math.exp(vector[i])).sum();
		return IntStream.range(0,  vector.length).mapToDouble(i -> Math.exp(vector[i]) / sum).toArray();
	}

	public double getMean(double[][] values, int axis) {
		return IntStream.range(0, values.length).mapToDouble(i -> values[i][axis]).average().getAsDouble();
	}

	public double getMean(double[][] values) {
		return getMean(values, 0);
	}

	public int argMax(double[] values) {
		final double max = Arrays.stream(values).max().getAsDouble();
		return IntStream.range(0, values.length).map(i -> (values[i] == max) ? i : 0).sum();
	}

	@Classification(message = "Confusion Matrix")
	public double getAccuracy(double[][] expected, double[][] prediction) {
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
		return printConfusinMatrix(matrix, new String[] { "A", "B", "C" });
	//	return matrix;
	}
	
	@Classification(message = "Mean Category Cross-Entropy")
	public double getMCCE(double[][] expected, double[][] prediction) {
		double sum = 0;
		for(int i = 0; i < expected.length; i++) {
			for(int j = 0; j < expected[i].length; j++) {
				sum += -1 * (expected[i][j] * Math.log(prediction[i][j] + Math.pow(10,  -100)) / expected[i].length);
			}
		}
		return sum / expected.length;
	}

	public double printConfusinMatrix(int[][] matrix, String[] labels) {
		int col = 15;
		String format = "%-" + col + "s";
		String format1 = "%-" + col + "d";
		String format2 = "%-" + col + ".2f";
		System.out.println("-Confusion Matrix--------------------------------------------------");
		System.out.printf(format, "Features");
		for (int i = 0; i < labels.length; i++) {
			System.out.printf(format, labels[i]);
		}
		for (int i = 0; i < matrix.length; i++) {
			System.out.printf("\n" + format, labels[i]);
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.printf(format1, matrix[i][j]);
			}
		}
		System.out.print("\n--------------------------------------------------------------------\n");
		System.out.printf(format, "");
		String[] metrics = { "Precision(%)", "Recall(%)", "F1-Score(%)", "Support" };
		for (int i = 0; i < metrics.length; i++) {
			System.out.printf(format, metrics[i]);
		}
		double[][] scores = new double[matrix.length][metrics.length];
		for (int i = 0; i < matrix.length; i++) { // order by row
			System.out.printf("\n" + format, labels[i]);
			final int I = i;
			double sumRow = Arrays.stream(matrix[i]).sum();
			double sumCol = IntStream.range(0, matrix[i].length).mapToDouble(z -> matrix[z][I]).sum();
			double precision = 0, recall = 0;
			for (int j = 0; j < metrics.length; j++) {
				if (j == 0) { // Precision
					precision = matrix[i][i] / (sumCol == 0 ? 1 : sumCol);
					scores[i][j] = precision;
					System.out.printf(format2, precision * 100);
				} else if (j == 1) {
					recall = matrix[i][i] / (sumRow == 0 ? 1 : sumRow);
					scores[i][j] = recall;
					System.out.printf(format2, recall * 100);
				} else if (j == 2) {
					double f1 = 2 * (precision * recall) / ((precision + recall) == 0 ? 1 : (precision + recall));
					scores[i][j] = f1;
					System.out.printf(format2, f1 * 100);
				} else {
					int support = (int) sumRow;
					scores[i][j] = support;
					System.out.printf(format1, support);
				}
			}
		}
		System.out.print("\n--------------------------------------------------------------------");
		String[] titles = { "Accuracy", "Macro Avg.", "Weighted Avg" };
		double weighted_avg = 0;
		for (int i = 0; i < titles.length; i++) {
			final int I = i;
			System.out.printf("\n" + format, titles[i]);
			for (int j = 0; j < metrics.length; j++) {
				final int J = j;
				int total = IntStream.range(0, matrix.length).map(z -> Arrays.stream(matrix[z]).sum()).sum();
				if (i == 0) {
					if (j == (scores.length - 1)) {
						double accuracy = (double) IntStream.range(0, matrix.length).map(z -> matrix[z][z]).sum()
								/ total;
						System.out.printf(format2, accuracy * 100);
					} else if(j == (metrics.length - 1)) {
						System.out.printf(format1, total);
					}
					else {
						System.out.printf(format, "");
					}
				} else if(i == 1){
					if (j == (metrics.length - 1)) {
						System.out.printf(format1, total);
					} else {
						double value = Arrays.stream(scores).map(arr -> arr[J]).mapToDouble(Double::doubleValue).average().getAsDouble();
						System.out.printf(format2, value * 100);
					}
				} else if(i == 2) {
					if (j == (metrics.length - 1)) {
						System.out.printf(format1, total);
					} else {
						double value = Arrays.stream(scores).map(arr -> arr[J] * arr[scores[J].length - 1]).mapToDouble(Double::doubleValue).sum();
						System.out.printf(format2, 100 * value / total);
						if(j == metrics.length - 2) {
							weighted_avg = 100 * value / total;
						}
					}
				}
			}
		}
		System.out.println("\n--------------------------------------------------------------------\n");
		return weighted_avg;
		//return metrics
	}

	public void scoreModel(double[][] expected, double[][] prediction, ModelType type)
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
				double result = (double)method.invoke(this, expected, prediction);
				values.put(message, result);
			}
		}
		for(var kv : values.entrySet()) {
			System.out.printf("%-30s\t%.4f\n", kv.getKey(), kv.getValue());
		}
	}
}
