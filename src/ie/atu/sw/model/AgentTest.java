package ie.atu.sw.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AgentTest {
	Agent agent;
	
	@Before
	public void beforeTest() {
		agent = new FuzzyAgent(30, 20, 15, 3);
	}
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testGetColumn() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testExtractSample() {
		fail("Not yet implemented");
	}

	@Test
	public void testSample() {
		fail("Not yet implemented");
	}


	@Test
	public void testPredict() {
		double[] aa = {		
				0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	
				1,	1,	1,	0,	0,	0,  0,	0,	0,	0,  0,	0,	0,	0,	1,	1,	1,	1,	1,	1,	
				1,	1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,  1,	1,	1,	1,	1,	1,	1,	
				1,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	1,	1,	1,	1,	1
		};
		
		double[] aaa = {		
				1, 1, 1, 0, 0, 1, 1, 
				1, 1, 0, 0, 0, 1, 1, 
				1, 1, 0, 0, 1, 1, 1
		};
		
		agent.decipher(aaa, 3, 7);
		agent.predict(3);
	//	fail("Not yet implemented");
	}

}