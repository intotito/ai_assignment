package ie.atu.sw;

import static java.lang.System.out;

import jhealy.aicme4j.NetworkBuilderFactory;
import jhealy.aicme4j.net.Activation;
import jhealy.aicme4j.net.Loss;

public class PilotModel extends TestPlatform{
	public PilotModel(int seed) throws Exception{
		this.seed = seed;
		
	}
	
	public PilotModel train(String fileName) throws Exception {
		long time = System.currentTimeMillis();
		out.println("Network Training Started ... ");
		network = NetworkBuilderFactory.getInstance().newNetworkBuilder()
				.inputLayer("Input", 80)
				.hiddenLayer("Hidden1", Activation.TANH, 24)
				.hiddenLayer("Hidden2", Activation.TANH, 10)
				.outputLayer("Output", Activation.TANH, 3)
				.train(trainX, trainExpected, 0.001, 0.95, 100_000, 0.0001, Loss.SSE)
				.save("./" + fileName).build();
		out.print("Model Finished Training in " + (System.currentTimeMillis() - time) / 1000 + " Seconds\n");

		return this;
	}
	
	public static void main(String[] arg) throws Exception{
		new PilotModel(8058)
		.loadData("./train_pilot.csv", 80, 3, 0.75)
//		.train("./pilot_tan_tan")
		.loadModel("./pilot_tan_tan")
		.testTrain(false)
		.testTest(false);
	}
}
