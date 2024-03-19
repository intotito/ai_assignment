package ie.atu.sw.model;

import jhealy.aicme4j.net.Aicme4jUtils;
import jhealy.aicme4j.net.NeuralNetwork;
import jhealy.aicme4j.net.Output;

public class NeuralNetworkAgent extends Agent{
	private static final String MODEL_PATH = "./resources/gridSearch/Signet7.ann";
	private NeuralNetwork network;
	public NeuralNetworkAgent(int STAGE_WIDTH, int STAGE_HEIGHT, int PLAYER_COLUMN, int horizon) {
		super(STAGE_WIDTH, STAGE_HEIGHT, PLAYER_COLUMN, horizon);
		try {
			network = Aicme4jUtils.load(MODEL_PATH);
			System.out.println(network.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int predict(int player_row) {
		int answer = 0;
		try {
			answer = (int) network.process(extractSample(player_row), Output.LABEL_INDEX);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer == 0 ? -1 : (answer == 1 ? 0 : 1);
	}

}
