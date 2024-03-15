package ie.atu.sw.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.IntStream;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class FuzzyAgent extends Agent {
	private FIS fis;
	private FunctionBlock fb;
	public FuzzyAgent(int STAGE_WIDTH, int STAGE_HEIGHT, int PLAYER_COLUMN, int horizon) {
		super(STAGE_WIDTH, STAGE_HEIGHT, PLAYER_COLUMN, horizon);
		try {
			fis = FIS.load(new FileInputStream("./Pilot.txt"), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		fb = fis.getFunctionBlock("Pilot");
		JFuzzyChart.get().chart(fb);

	}
	
	public static void main(String[] aa) {
		var fzzy = new FuzzyAgent(30, 20, 15, 3);
	}
	

	@Override
	public int predict(int player_row) {
	//	System.out.println("How far ----------------------------------------------------");
		double[] sample = extractSample(player_row);
		decipher(sample, 3, 7);
		int datum = 3;
		double[] softMax = {0.715, 0.195, 0.09};
		double[] tBag = new double[width + 1];
		double[] bBag = new double[width + 1];
		tBag[0] = 0;
		bBag[0] = 0;
		for(int i = 0; i < width; i++) {
			int top = 0, bottom = 0;
			boolean reset = false;
			for(int j = 0; j < height; j++) {
				double value = sample[i * height + j];
				if(reset) {
					if(value == 1) {
						bottom++;
					}
				} else {
					if(value == 1) {
						top++;
					} else {
						reset = true;
						bottom = 0;
					}
				}
			}
			bottom = bottom - (datum + 1);
			top = (datum + 1) - top;
			tBag[i + 1] = top;// * softMax[i];
			bBag[i + 1] = bottom;// * softMax[i];
			System.out.println("Top[" + i + "] = " + top);
			System.out.println("Bottom[" + i + "] = " + bottom);
			
		}
		double top_vec = IntStream.range(0,  width).mapToDouble(i -> Math.toDegrees(softMax[i] * Math.atan(tBag[i + 1] - tBag[i]))).sum();
		double bottom_vec = IntStream.range(0,  width).mapToDouble(i -> Math.toDegrees(softMax[i] * Math.atan(bBag[i + 1] - bBag[i]))).sum();
		
		System.out.println("Top_Vec: " + top_vec);
		System.out.println("Bottom_Vec: " + bottom_vec);
		
	//	JFuzzyChart.get().chart(fb);
		fb.setVariable("top_vec", top_vec);
		fb.setVariable("bottom_vec", bottom_vec);
		fb.evaluate();
		Variable tip = fb.getVariable("direction");
		double ans = Math.round(tip.defuzzify());
		System.out.println("Evaluation: " + ans);
		
		return (int)ans;
	}

}
