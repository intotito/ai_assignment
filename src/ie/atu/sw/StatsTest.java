package ie.atu.sw;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import ie.atu.sw.stat.ModelType;
import ie.atu.sw.stat.Stats;

public class StatsTest {
	static Stats stats;
	
	
	static double[][] y = {
		{4}, {6.8}, {11.6}, {22.5}, {18.6}, {27.4}, {29.9}, {36}, {38.3}, {4.6}, {16.7}, {26.0}, {37}, {37.8}, {2}, {40.2},
	};
	static double[][] y_ = {
		{4.1593}, {9.791}, {12.6069}, {18.2387}, {21.0546}, {26.6863}, {29.5022}, {32.3181}, {40.7657}, {6.9751}, {15.4228}, 
		{23.8704}, {35.134}, {37.9499}, {1.3434}, {43.5816}
			
	};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		stats = new Stats();
	}

	@Test
	public void testGetMSE() {
		double expected = 5.10319;
		double mse = stats.getMSE(y, y_);
		System.out.println("MSE : " + mse);
		assertEquals(mse, expected, 0.01);
	}

	@Test
	public void testGetRMSE() {
		double expected = 2.25902;
		double rmse = stats.getRMSE(y, y_);
		System.out.println("RMSE : " + rmse);
		assertEquals(rmse, expected, 0.01);
	}

	@Test
	public void testGetSSE() {
		double expected = 81.6517;
		double sse = stats.getSSE(y, y_);
		System.out.println("SSE : " + sse);
		assertEquals(sse, expected, 0.01);
	}

	@Test
	public void testGetSSR() {
		double expected = 2695.9258;
		double ssr = stats.getSSR(y, y_);
		System.out.println("SSR : " + ssr);
		assertEquals(ssr, expected, 0.01);
	}

	@Test
	public void testGetMAE() {
		double expected = 1.87301;
		double mae = stats.getMAE(y, y_);
		System.out.println("MAE : " + mae);
		assertEquals(mae, expected, 0.01);
	}

	@Test
	public void testGetRSquared() {
		double expected = 0.9706;
		double r2 = stats.getRSquared(y, y_);
		System.out.println("R2: " + expected);
		assertEquals(expected, r2, 0.001);
	}

	@Test
	public void testGetARSquared() {
		double expectedR2 = 0.96850;
		double answer = stats.getARSquared(y, y_);
		System.out.println("Adjusted R2: " + answer);
		assertEquals(expectedR2, answer, 0.001);
	}

	@Test
	public void testGetMeanDoubleArrayArrayInt() {
		double expected = 22.4625;
		double mean = stats.getMean(y, 0);
		System.out.println("Mean(): " + mean);
		assertEquals(mean, expected, 0.01);
	}

	@Test
	public void testGetMeanDoubleArrayArray() {
		double expected = 22.4625;
		double mean = stats.getMean(y_);
		System.out.println("Mean(0): " + mean);
		assertEquals(mean, expected, 0.01);
	}

	@Test
	public void testScoreModel() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		System.out.println("- Score: ------------------------------------------------------------");
		stats.scoreModel(y_, y, ModelType.REGRESSION);
		System.out.println("----------------------------------------------------------------------");
	}
	
	@Test 
	public void testArgMax() {
		int expected = 0;
		double[] testValues = {10.03, 0.92, 3.03};
		int argMax = stats.argMax(testValues);
		System.out.println("ArgMax: "  + argMax);
		assertEquals(expected, argMax);
	}
	
	@Test
	public void testGetMCCE() {
		double[][] trueValue = {{0, 1, 0, 0}};
		double[][] predValue = {{0.05, 0.85, 0.10, 0.0}};
		double expected = 0.16251892949777494;
		double crossE = stats.getMCCE(trueValue, predValue);
		assertEquals(expected, crossE, 0.001);
		
	}
	
	@Test
	public void testSoftMax() {
		double[] testData = {0.000, 0.367, 0.695};
		double[] expected = {0.2248652892, 0.3245700905, 0.4505646203};
		double[] softMax = stats.softMax(testData);
		System.out.println("Expected: " + Arrays.toString(expected));
		System.out.println("SoftMax: " + Arrays.toString(softMax));
		assertArrayEquals(expected, softMax, 0.001);
		
	}
	
/*	@Test
	public void testGetConfusionMatrix() {
		double[][] ex = {{0, 0, 3}, {1, 0, 0}, {0, 0, 1}, {0, 0, 1}, {1, 0, 0}, {0, 1, 0}};
		double[][] pre = {{1, 0, 0}, {1, 0, 0}, {0, 0, 1},{0, 0, 1}, {1, 0, 0}, {0, 0, 1}};
		int[][] expected = {{2, 0, 0}, {0, 0, 1}, {1, 0, 2}};
		int[][] matrix = stats.getConfusionMatrix(ex, pre);
		for(int i = 0; i < matrix.length; i++) {
			System.out.println(Arrays.toString(matrix[i]));
			assertTrue(Arrays.equals(matrix[i], expected[i]));
		}
	}
*/}
