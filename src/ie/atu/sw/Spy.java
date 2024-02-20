package ie.atu.sw;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jhealy.aicme4j.net.Aicme4jUtils;
import jhealy.aicme4j.net.NeuralNetwork;
import jhealy.aicme4j.net.Output;

import java.io.File;

public class Spy {
	private boolean recording = false;
	private BufferedWriter writer;
	private int width, height;
	private int PLAYER_COLUMN = 15;
	private NeuralNetwork network;

	public Spy(int width, int height) {
		this.width = width;
		this.height = height;
		try {
			network = Aicme4jUtils.load("./pilot_best_tune1");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getTitle(int predSize, int i) {
		if (i < height) {
			return "cord_" + String.format("%02d", i);
		} else {
			return "col_" + String.format("%02d_%02d", i / height, i - (height * (i / height)));
		}
	}

	public void recordSample(double[] sample) {
		try {
			writer.append(IntStream.range(0, sample.length).mapToObj(i -> String.format("%.2f", sample[i]))
					.collect(Collectors.joining(",")));
			writer.append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopRecording() {
		if (!recording)
			return;
		try {
			writer.close();
			recording = false;
			System.out.println("Stop Recording...");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startRecording(int predSize) {
		if (recording)
			return;
		try {
			File file = new File("./train_pilot.csv");
			StringBuffer ss = new StringBuffer();
			if (!file.exists()) {
				ss.append(IntStream.range(0, (predSize + 1) * height).mapToObj(i -> getTitle(predSize, i))
						.collect(Collectors.joining(",")));
				ss.append(",");
				ss.append(Arrays.stream(new String[] { "Up", "None", "Down" }).collect(Collectors.joining(",")));
				ss.append("\n");
				System.out.println("Awumen" + ss.toString());
			}
			writer = new BufferedWriter(new FileWriter(file, true));
			writer.append(ss.toString());
			recording = true;
			System.out.println("Recording Started..." + ss.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void decipher(double[] sample) {
		for (int i = 0; i < sample.length; i++) {
			int y = (height * i) - (i / width) * (width * height - 1);
			System.out.printf("%d%c", (int) sample[y], (((i + 1) % (width) != 0) || i == 0) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}

	public void decipher(double[] sample, int cols) {
		for (int i = 0; i < cols * height; i++) {
			int y = (height * i) - (i / cols) * (cols * height - 1);
			System.out.printf("%d%c", (int) sample[y], (((i + 1) % (cols) != 0) || i == 0 && cols != 1) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}

	public int getColumn(int index) {
		return (index / height);
	}

	public int getTop(int col, double[] sample) {
		return (int) Arrays.stream(sample).skip(col * height).mapToLong(Math::round).takeWhile(i -> i == 1).count();
	}

	public int getBottom(int col, double[] sample) {
		int count = 0;
		for (int i = 0; i < height; i++) {
			int index = col * height + height - 1 - i;
			if (sample[index] == 1) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	public double[] getDerivatives(double[] sample, int width) {
		double[] value = new double[width - 1];
		for (int i = 0; i < value.length; i++) {
			int column = i;
			int top = getTop(column, sample);
			int bottom = getBottom(column, sample);

//			int top1 = getTop(column + 1, sample);
//			int bottom1 = getBottom(column + 1, sample);

//			int bias = (top1 - top) - (bottom1 - bottom);

			// int target = height - (top + (height - (top + bottom)) / 2 - bias);
			int target = top + (height - (top + bottom)) / 2;
//			int target1 = top1 + (height - (top1 + bottom1)) / 2;

//			System.out.println("Top: " + top + " Bottom: " + bottom + " \tBias: " + bias + " Target: " + target);
			value[i] = target;
		}
		return value;
	}

	public double[] getNextColumn(int index, double[] sample, int width) {
		int c = getColumn(index);
		int i = height * (c + 1);
		// System.out.println("Column: " + c);
		double[] value = new double[height * width];
		System.arraycopy(sample, i, value, 0, height * width);
		return value;
	}

	public int getIndex(int row, int col) {
		int index = col * height + row;
		// System.out.println("Index: " + index);
		return index;
	}

	public int modelPredict(double[] sample, int player_row, int player_col, int predSize) {
		try {
			double[] player_pos = IntStream.range(0, height).mapToDouble(i -> i == player_row ? 1 : 0).toArray();
			double[] data = new double[height * (predSize + 1)];
			System.arraycopy(player_pos, 0, data, 0, height);
			System.arraycopy(sample, (player_col + 1) * height, data, height, height * predSize);
			int answer = (int) network.process(data, Output.LABEL_INDEX);
			return answer == 0 ? -1 : (answer == 1 ? 0 : 1);
		} catch (Exception e) {
			throw new IllegalArgumentException("The man died!!!");
		}
	}

	public int predict(double[] sample, int player_row, int player_col, int predSize, double[] weights) {
		double[] suggestions = getDerivatives(getNextColumn(getIndex(player_row, player_col), sample, predSize),
				predSize);
		double pp = IntStream.range(0, suggestions.length).mapToDouble(i -> weights[i] * suggestions[i]).sum();
		double pred = pp;// < 0 ? Math.floor(pp) : Math.ceil(pp);
		int answer = ((int) (pred - player_row) == 0) ? 0
				: (int) ((int) (pred - player_row) / Math.abs((int) (pred - player_row)));
		// System.out.println("Answer: " + answer + " Was: " + (pred - player_row) + "
		// Player row: " + player_row + " Pred: " + pred);
		if (recording) {
			double[] player_pos = IntStream.range(0, height).mapToDouble(i -> i == player_row ? 1 : 0).toArray();
			double[] data = new double[height * (predSize + 1) + 3];
			double[] y = new double[] { answer == -1 ? 1 : 0, answer == 0 ? 1 : 0, answer == 1 ? 1 : 0 };
			System.arraycopy(player_pos, 0, data, 0, height);
			System.arraycopy(sample, (player_col + 1) * height, data, height, height * predSize);
			decipher(data, predSize + 1);
			System.arraycopy(y, 0, data, height * (predSize + 1), y.length);
			recordSample(data);
		}
		return answer;
	}

	public static void mains(String[] args) {
		Spy spy = new Spy(30, 20);
//		spy.decipher(sample);
		/*
		 * double[] cc = spy.getNextColumn(spy.getIndex(11, 15), sample, 10);
		 * //Arrays.stream(cc).mapToLong(Math::round).forEach(System.out::println);
		 * spy.decipher(cc, 10 ); spy.getDerivatives(cc, 10);
		 * 
		 * double[] weights = new double[]{0.5, 0.25, 0.12, 0.08, 0.03, 0.02, 0.01,
		 * 0.01, 0.0}; double[] pred = spy.predict(sample, 11, 15, 10, weights); double
		 * answer = IntStream.range(0, pred.length).mapToDouble(i -> weights[i] *
		 * pred[i]).sum();
		 * Arrays.stream(pred).mapToLong(Math::round).forEach(System.out::println);
		 * System.out.println("Answer: " + answer); System.out.println("Please Press: "
		 * + (int)(answer - 11));
		 */
	}
}
