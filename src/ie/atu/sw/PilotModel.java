package ie.atu.sw;

import static java.lang.System.out;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import ie.atu.sw.stat.ModelType;
import ie.atu.sw.stat.Stats;
import jhealy.aicme4j.NetworkBuilderFactory;
import jhealy.aicme4j.net.Activation;
import jhealy.aicme4j.net.Loss;

public class PilotModel extends TestPlatform {



	public PilotModel(int seed) throws Exception {
		this.seed = seed;

	}

	/**
	 * Train a single neural network
	 * @param fileName name of the file to save the network
	 * @return self
	 */
	public PilotModel train(String fileName) throws Exception {
		long time = System.currentTimeMillis();
		out.println("Network Training Started ... ");
		network = NetworkBuilderFactory.getInstance().newNetworkBuilder().inputLayer("Input", 21)
				// .hiddenLayer("Hidden1", Activation.TANH, 24)
				// .hiddenLayer("Hidden2", Activation.TANH, 10)
				.hiddenLayer("Hidden3", Activation.RELU, 12).outputLayer("Output", Activation.RELU, 3)
				.train(trainX, trainExpected, 0.001, 0.95, 700, 0.001, Loss.CEE).save("./" + fileName).build();
		out.print("Model Finished Training in " + (System.currentTimeMillis() - time) / 1000 + " Seconds\n");

		return this;
	}
	/**
	 * Train batch of neural networks
	 * @param fileName name pattern of the file to save the networks
	 * @return self
	 * @throws Exception
	 */
	public PilotModel trainBatch(String fileName) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		var writer = new BufferedWriter(new FileWriter("./resources/data/" + fileName + ".csv", true));
		Consumer<String> write = (String s) -> {
			executor.execute(() -> {
				try {
					writer.write(s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		};

		Stats stats = new Stats();
		
		loadData("./train_data_3_by_7_1.00.csv", 21, 3, 0.01);
		int[] hiddenLayers = { 1, 2 };
		int[][] hiddenNeurons = { { 12, 15, 20 }, { 15, 20, 45 } };
		Activation[] aFs = { Activation.ISRLU, Activation.TANH, Activation.RELU };
		Loss[] lFs = { Loss.CEE, Loss.MSE, Loss.SSE };
		double[] alphas = { 0.01, 0.001, 0.0001 };
		double[] betas = { 0.5, 0.75, 0.95 };
		double[] mErr = { 0.0001, 0.00001 };
		int[] epochs = { 500, 300 };
		int kValidationFold = 5;

		var ss = gridSearch(testX, testExpected, kValidationFold, hiddenLayers, hiddenNeurons, aFs, lFs, alphas, betas, mErr, epochs);
		ss = Arrays.stream(ss).filter(Objects::nonNull).toArray(ModelInfo[]::new);
		for (int i = 0; i < ss.length && ss[i] != null; i++) {
			double[][] qq = ss[i].scores();
			StringBuffer sb = new StringBuffer(ss[i].networkInfo());
			sb.append(",").append(String.format("%.2f%c, %.4f\n", stats.getMean(qq), '%', stats.getMean(qq, 1)));
			System.out.printf("[%s]\tScore: %.2f%c\tError: %.4f\n", ss[i].networkInfo(), stats.getMean(qq), '%',
					stats.getMean(qq, 1));
			write.accept(sb.toString());
		}
		executor.shutdown();
		return this;
	}

	public static void main(String[] arg) throws Exception {
/*
 * 		Load and test Model
 */
		new PilotModel(31)
		.loadData(TRAINING_DATA, 21, 3, 0.75)
		.loadModel(GRID_PATH + "1120.ann")	
		.testModel(ModelType.CLASSIFICATION, true);

/*
*		 ** Grid Search **
*    	 *****************		
		new PilotModel(7739)
		.trainBatch("report");
*/


 /* 		** Train Single Model **
 * 		************************ 
 		new PilotModel(7739)
 		.loadData(TRAINING_DATA, 21, 3, 0.75)
 		.train("modelXX1.ann")
 		.testModel(ModelType.CLASSIFICATION, true);
*/ 
	}
}
