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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ie.atu.sw.stat.Stats;
import jhealy.aicme4j.NetworkBuilderFactory;
import jhealy.aicme4j.net.Activation;
import jhealy.aicme4j.net.LayerSize;
import jhealy.aicme4j.net.Loss;

public class PilotModel extends TestPlatform {

	private record ModelInfo(String networkInfo, long time, double[][] scores) implements Comparable<ModelInfo> {
		public int compareTo(ModelInfo a) {
			Stats stats = new Stats();
			return (int) (stats.getMean(a.scores(), 0) - stats.getMean(scores(), 0));
		}
	}

	public PilotModel(int seed) throws Exception {
		this.seed = seed;

	}

	public ModelInfo[] gridSearch(double[][] testDataX, double[][] expectedY, int[] hiddenLayers, int[][] hiddenNeurons,
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

									// int aww = (hiddenNeurons[i].length - hiddenLayers[i] + 1);
									// System.out.println("Awumen " + aww);
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
										double[][] score = crossValidate(testDataX, expectedY, 5, seed, network, false);
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

	public void trainBatch() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		var writer = new BufferedWriter(new FileWriter("./resources/data/report.csv", true));
		Consumer<String> write = (String s) -> {
			executor.execute(() -> {
				try {
					writer.write(s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
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

		// trainX, expectedX
		var ss = gridSearch(testX, testExpected, hiddenLayers, hiddenNeurons, aFs, lFs, alphas, betas, mErr, epochs);
		ss = Arrays.stream(ss).filter(Objects::nonNull).toArray(ModelInfo[]::new);
//		Arrays.sort(ss);
//		System.out.println(Arrays.toString(ss));
		for (int i = 0; i < ss.length && ss[i] != null; i++) {
//			System.out.printf("\n%s, Time: %.2fs\n", ss[i].networkInfo(), ss[i].time() / 1000.0);
			double[][] qq = ss[i].scores();
//			String axx = IntStream.range(0, qq.length).mapToObj(k -> String.format("(%.2f: %.4f)", qq[k][0], qq[k][1]))
//					.collect(Collectors.joining(","));
			StringBuffer sb = new StringBuffer(ss[i].networkInfo());
			sb.append(",").append(String.format("%.2f%c, %.4f\n", stats.getMean(qq), '%', stats.getMean(qq, 1)));
			System.out.printf("[%s]\tScore: %.2f%c\tError: %.4f\n", ss[i].networkInfo(), stats.getMean(qq), '%',
					stats.getMean(qq, 1));
			write.accept(sb.toString());
		}
		executor.shutdown();
	}

	public static void main(String[] arg) throws Exception {
		new PilotModel(31)
//		.trainBatch();
		.loadData("./train_data_3_by_7_1.00.csv", 21, 3, 1)
//		.train("./model_3_by_7_1.03")
		.loadModel("./resources/gridSearch/grid_model1120.ann")
//		.testTrain(true);
//		.testTest(true)
		.crossValidate(5, 3232, true);
	}
}
