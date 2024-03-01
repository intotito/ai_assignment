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
				.inputLayer("Input", 21)
		//		.hiddenLayer("Hidden1", Activation.TANH, 24)
		//		.hiddenLayer("Hidden2", Activation.TANH, 10)
				.hiddenLayer("Hidden3", Activation.RELU, 12)
				.outputLayer("Output", Activation.RELU, 3)
				.train(trainX, trainExpected, 0.001, 0.95, 700, 0.001, Loss.CEE)
				.save("./" + fileName).build();
		out.print("Model Finished Training in " + (System.currentTimeMillis() - time) / 1000 + " Seconds\n");

		return this;
	}
	
	public void trainBatch() {
		
	}
	
	public static void main(String[] arg) throws Exception{
		new PilotModel(313)
		.loadData("./trainXXX_data_3_by_7_1.00.csv", 21, 3, 1)
//		.train("./model_3_by_7_1.03")
		.loadModel("./model_3_by_7_1.02")
//		.testTrain(true);
//		.testTest(true)
		.crossValidate(5, 3232);
	}
}
