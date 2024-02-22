package ie.atu.sw;

import static java.lang.System.out;

import jhealy.aicme4j.NetworkBuilderFactory;
import jhealy.aicme4j.net.Activation;
import jhealy.aicme4j.net.LayerSize;
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
		//		.hiddenLayer("Hidden2", Activation.TANH, 10)
				.hiddenLayer("Hidden3", Activation.TANH, 20)
				.outputLayer("Output", Activation.TANH, 3)
				.train(trainX, trainExpected, 0.01, 0.95, 500, 0.00001, Loss.SSE)
				.save("./" + fileName).build();
		out.print("Model Finished Training in " + (System.currentTimeMillis() - time) / 1000 + " Seconds\n");

		return this;
	}
	
	public static void main(String[] arg) throws Exception{
		new PilotModel(31)
		.loadData("./train_pilot.csv", 80, 3, 0.85)
//		.train("./pilot_best_tune8")
		.loadModel("./pilot_best_tune2")
		.testTrain(false)
		.testTest(true);
	}
}
