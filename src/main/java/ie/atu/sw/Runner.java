package ie.atu.sw;

import javax.swing.SwingUtilities;

import ie.atu.sw.stat.ModelType;

import static java.lang.System.*;

import java.io.File;
import java.util.Arrays;

public class Runner {
	public static void main(String[] args) throws Exception {

		if (args == null || args.length == 0) {
			SwingUtilities.invokeAndWait(() -> { // Sounds like the Command Pattern at work!
				try {
					new GameWindow();
				} catch (Exception e) {
					out.println("[ERROR] Yikes...problem starting up " + e.getMessage());
				}
			});
		} else {
			// join all arguments
			String joinedArgs = String.join(" ", args);
			// break strings of form -key1 value1 -key2 value2 into key1 value1 key2 value2
			String[] argz = joinedArgs.split(" -");
			Arrays.stream(argz).forEach(String::trim);
			for (int i = 0; i < argz.length; i++) {
				ModelSelection model = new ModelSelection(1);
				// flag is the first word with hyphen
				String flag = argz[i].split(" ")[0];
				String action = argz[i].split(" ")[1];
				if (flag.equals("-test")) {
					if (action.equals("all")) {
						model.testAllModel("test_report");
					} else {
						File file = new File(action);
						if (!file.exists()) {
							out.println("File " + action + " does not exist or is not a valid ACME4J Neural Network file");
							return;
						}
						model.loadData(ModelSelection.TRAINING_DATA, 21, 3, 0.0)
						.loadModel(action)
						.testModel(ModelType.CLASSIFICATION, true);
					}
				} else if (flag.equals("-train")){
					if(action.equals("all")){
						model.trainBatch("batch_report");
					} else{
						model.loadData(ModelSelection.TRAINING_DATA, 21, 3, 0.75)
						.train(action)
						.testModel(ModelType.CLASSIFICATION, true);
					}
				}
			}
		}
		/*
		 * Always run a GUI in a separate thread from the main thread.
		 */

	}
}