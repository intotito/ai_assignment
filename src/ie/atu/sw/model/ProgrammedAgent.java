package ie.atu.sw.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * This class is a subclass of the Agent and implements the functionality required to navigate 
 * through the Stage using traditional programming methodology. Also implements the Trainer interface
 * and implements all the required method to record training data. 
 */
public class ProgrammedAgent extends Agent implements Trainer{
	/**
	 * Version major.minor
	 */
	private double version = 1.1;
	/**
	 * Determines if the Trainer is currently recording
	 */
	private boolean recording = false;
	/**
	 * BufferedWriter for writing to file
	 */
	private BufferedWriter writer;
	/**
	 * Name of the file to be written to
	 */
	private String fileName;
	/**
	 * Stored softmax weight evaluated once to avoid overhead
	 */
	private double[] weights;
	/**
	 * ExecutorService for writing data to file
	 */
	private ExecutorService executor;

	/**
	 * {@inheritDoc}
	 */
	public ProgrammedAgent(int STAGE_WIDTH, int STAGE_HEIGHT, int PLAYER_COLUMN, int horizon) {
		super(STAGE_WIDTH, STAGE_HEIGHT, PLAYER_COLUMN, horizon);
		fileName = String.format("trainXXX_data_%d_by_%d_%.2f", horizon, getHeight(), version);
		double[] arr = IntStream.range(0,  getWidth()).mapToDouble(i -> getWidth()- i + 1).toArray();
		weights = softMax(arr);
	}
	
	/**
	 * Write data to file
	 * @param data to be written to file
	 */
	private void write(String data) {
		executor.execute( () -> {
			try {
				writer.append(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	/**
	 * Initializes I/O resources and ExecutorService for writing to file.
	 * Will return immediately without effect if currently recording
	 */
	public void startRecording() {
		if (recording)
			return;
		try {
			executor = Executors.newFixedThreadPool(4);
			File file = new File(String.format("%s%s%s", "./", fileName, ".csv"));
			StringBuffer ss = new StringBuffer();
			if (!file.exists()) {
				ss.append(IntStream.range(0, getWidth() * getHeight()).mapToObj(i -> String.format("%s%02d", "F_", i + 1))
						.collect(Collectors.joining(",")));
				ss.append(",");
				ss.append(Arrays.stream(new String[] { "Up", "None", "Down" }).collect(Collectors.joining(",")));
				ss.append("\n");
			}
			writer = new BufferedWriter(new FileWriter(file, true));
			write(ss.toString());
			recording = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Recording Started...\n");
	}

	@Override
	/**
	 * Shuts down ExecutorService and closes all I/O resources. 
	 * Will return immediately without effect if not recording. 
	 */
	public void stopRecording() {
		if (!recording)
			return;
		try {
			executor.shutdown();
			writer.close();
			recording = false;
			System.out.println("Stop Recording...");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Writes the sample to file or 
	 * do nothing if not in recording state. 
	 * @param sample to be written to file
	 */
	private void record(double[] sample) {
		StringBuffer sb = new StringBuffer();
		sb.append(IntStream.range(0, sample.length).mapToObj(i -> String.format("%.2f", sample[i]))
				.collect(Collectors.joining(",")));
		sb.append("\n");
		write(sb.toString());
	}
	
	/**
	 * Softmax Function
	 * @param vector to be evaluated
	 * @return softmax of the vector
	 */
	private double[] softMax(double[] vector) {
		double sum = IntStream.range(0, vector.length).mapToDouble(i -> Math.exp(vector[i])).sum();
		return IntStream.range(0,  vector.length).mapToDouble(i -> Math.exp(vector[i]) / sum).toArray();
	}
	/**
	 * Decision of next move to make is evaluated in this method. Prediction is based on the 
	 * move that takes the ship towards the center of the cave's opening.
	 * @param player_row the row the player occupies in the Stage
	 */
	@Override
	public int predict(int player_row) {
		double[] sample = extractSample(player_row);
		double sum = 0;
		for(int i = 0; i < getWidth(); i++) {
			double top = 0, bottom = 0;
			boolean reset = false;
			for(int j = 0; j < getHeight(); j++) {
				if(reset) {
					if(sample[i * getHeight()+ j] == 1) {
						bottom++;
					} 
				} else {
					if(sample[i * getHeight() + j] == 1) {
						top++;
					} else {
						reset = true;
					}
				}
			}
			double dot = weights[i] * (top + (getHeight() - top - bottom - 1) / 2);
			sum += dot;
		}
		int target = (int)Math.round(sum);
		int player_pos = getWidth();
		int decision = (target - player_pos < 0) ? -1 : (target - player_pos == 0 ? 0 : 1); 
		if(recording) {
			double[] write = IntStream.range(0, sample.length + getWidth()).mapToDouble(
					i -> ((i < sample.length) ? sample[i] : ((i - sample.length == decision + 1) ? 1 : 0)))
					.toArray();
			record(write);
		}
		return decision;
	}
	
	
}
