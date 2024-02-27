package ie.atu.sw.model;

public interface AutoPilot {
	
	public int predict(int player_row);
	public void sample(double[] sample);
	public boolean look();
	
}
