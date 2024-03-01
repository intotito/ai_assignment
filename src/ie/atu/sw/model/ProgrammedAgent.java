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

public class ProgrammedAgent extends Agent implements Trainer{
	private double version = 1.0;
	private boolean recording = false;
	private BufferedWriter writer;
	private String fileName;
	private double[] weights;
	private ExecutorService executor;

	public ProgrammedAgent(int STAGE_WIDTH, int STAGE_HEIGHT, int PLAYER_COLUMN, int horizon) {
		super(STAGE_WIDTH, STAGE_HEIGHT, PLAYER_COLUMN, horizon);
		fileName = String.format("trainXXX_data_%d_by_%d_%.2f", horizon, height, version);
		double[] arr = IntStream.range(0,  width).mapToDouble(i -> width - i + 1).toArray();
		weights = softMax(arr);
	}
	
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
	public void startRecording() {
		if (recording)
			return;
		try {
			executor = Executors.newFixedThreadPool(4);
			File file = new File(String.format("%s%s%s", "./", fileName, ".csv"));
			StringBuffer ss = new StringBuffer();
			if (!file.exists()) {
				ss.append(IntStream.range(0, width * height).mapToObj(i -> String.format("%s%02d", "F_", i + 1))
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

	@Override
	public void record(double[] sample) {
		StringBuffer sb = new StringBuffer();
		sb.append(IntStream.range(0, sample.length).mapToObj(i -> String.format("%.2f", sample[i]))
				.collect(Collectors.joining(",")));
		sb.append("\n");
		write(sb.toString());
	}
	
	public double[] softMax(double[] vector) {
		double sum = IntStream.range(0, vector.length).mapToDouble(i -> Math.exp(vector[i])).sum();
		return IntStream.range(0,  vector.length).mapToDouble(i -> Math.exp(vector[i]) / sum).toArray();
	}
	@Override
	public int predict(int player_row) {
		double[] sample = extractSample(player_row);
		double sum = 0;
		for(int i = 0; i < width; i++) {
			double top = 0, bottom = 0;
			boolean reset = false;
			for(int j = 0; j < height; j++) {
				if(reset) {
					if(sample[i * height + j] == 1) {
						bottom++;
					} 
				} else {
					if(sample[i * height + j] == 1) {
						top++;
					} else {
						reset = true;
					}
				}
			}
			double dot = weights[i] * (top + (height - top - bottom - 1) / 2);
			sum += dot;
		}
		int target = (int)Math.round(sum);
		int player_pos = width;
		int decision = (target - player_pos < 0) ? -1 : (target - player_pos == 0 ? 0 : 1); 
		if(recording) {
			double[] write = IntStream.range(0, sample.length + 3).mapToDouble(
					i -> ((i < sample.length) ? sample[i] : ((i - sample.length == decision + 1) ? 1 : 0)))
					.toArray();
			record(write);
		}
		return decision;
	}
	
	
	public void decipher(double[] sample, int cols, int height) {
		for (int i = 0; i < cols * height; i++) {
			int y = (height * i) - (i / cols) * (cols * height - 1);
			System.out.printf("%d%c", (int) sample[y], (((i + 1) % (cols) != 0) || i == 0 && cols != 1) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}
}
