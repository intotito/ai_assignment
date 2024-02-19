package ie.atu.sw;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Spy {
	
	private static double[] sample = {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 
			1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 
			1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 
			1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1
		};
	
	private int width, height;
	private int PLAYER_COLUMN = 15;
	
	public Spy(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void decipher(double[] sample) {
		for(int i = 0; i < sample.length; i++) {
			int  y = (height * i) - (i / width) * (width * height - 1);
			System.out.printf("%d%c", (int)sample[y], (((i + 1) % (width) != 0) || i == 0) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}
	
	public void decipher(double[] sample, int cols) {
		for(int i = 0; i < cols * height; i++) {
			int  y = (height * i) - (i / cols) * (cols * height - 1);
			System.out.printf("%d%c", (int)sample[y], (((i + 1) % (cols) != 0) || i == 0 && cols != 1) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}
	
	public int getColumn(int index) {
		return (index / height);
	}
	
	public int getTop(int col, double[] sample) {
		return (int)Arrays.stream(sample).skip(col * height).mapToLong(Math::round).takeWhile(i -> i == 1).count();
	}
	
	public int getBottom(int col, double[] sample) {
		int count = 0;
		for(int i = 0; i < height; i++) {
			int index = col * height + height - 1 - i;
			if(sample[index] == 1) {
				count++;
			} else { 
				break;
			}
		}
		return count;
	}
	
	public double[] getDerivatives(double[] sample, int width) {
		double[] value = new double[width - 1];
		for(int i = 0; i < value.length; i++) {
			int column = i;
			int top = getTop(column, sample);
			int bottom = getBottom(column, sample);
			
//			int top1 = getTop(column + 1, sample);
//			int bottom1 = getBottom(column + 1, sample);
			
//			int bias = (top1 - top) - (bottom1 - bottom);
			
			//int target =  height - (top + (height - (top + bottom)) / 2 - bias);
			int target = top + (height - (top + bottom)) / 2;
//			int target1 = top1 + (height - (top1 + bottom1)) / 2;
			
//			System.out.println("Top: " + top + " Bottom: " + bottom + " \tBias: " + bias + " Target: " + target);
			value[i] = target;
		}
		return value;
	}
	
	public double[] getNextColumn(int index, double[] sample, int width) {
		int c = getColumn(index);
		int i = height * (c + 1);
	//	System.out.println("Column: " + c);
		double[] value = new double[height * width];
		System.arraycopy(sample, i, value, 0, height * width);
		return value;
	}
	
	public int getIndex(int row, int col) {
		int index = col * height + row;
	//	System.out.println("Index: " + index);
		return index;
	}
	
	public int predict(double[] sample, int player_row, int player_col, int predSize, double[] weights) {
		double[] suggestions = getDerivatives(getNextColumn(getIndex(player_row, player_col), sample, predSize), predSize);
		double pp = IntStream.range(0,  suggestions.length).mapToDouble(i -> weights[i] * suggestions[i]).sum();
		double pred = pp;// < 0 ? Math.floor(pp) : Math.ceil(pp);
		int answer = ((int)(pred - player_row) == 0) ? 0 : (int)((int)(pred - player_row) / Math.abs((int)(pred - player_row)));
	//	System.out.println("Answer: " + answer + " Was: " + (pred - player_row) + " Player row: " + player_row + " Pred: " + pred);
		return answer;
	}
	
	public static void mains(String[] args) {
		Spy spy = new Spy(30, 20);
		spy.decipher(sample);
/*		double[] cc = spy.getNextColumn(spy.getIndex(11, 15), sample, 10);
		//Arrays.stream(cc).mapToLong(Math::round).forEach(System.out::println);
		spy.decipher(cc, 10 );
		spy.getDerivatives(cc, 10);
		
		double[] weights = new double[]{0.5, 0.25, 0.12, 0.08, 0.03, 0.02, 0.01, 0.01, 0.0};
		double[] pred = spy.predict(sample, 11, 15, 10, weights);
		double answer = IntStream.range(0,  pred.length).mapToDouble(i -> weights[i] * pred[i]).sum();
		Arrays.stream(pred).mapToLong(Math::round).forEach(System.out::println);
		System.out.println("Answer: " + answer);
		System.out.println("Please Press: " + (int)(answer - 11));
*/		
	}
}
