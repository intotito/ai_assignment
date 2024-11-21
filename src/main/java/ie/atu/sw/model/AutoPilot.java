package ie.atu.sw.model;
/**
 * The root interface of an AutoPilot Agent. This interface is provides the basic specification
 * for an agent to predict next move and sample data.
 */
public interface AutoPilot {
	/**
	 * Predict the next move to make. 
	 * @param player_row The current row the ship is currently occupying. 
	 * @return the move to make.
	 */
	public int predict(int player_row);
	/**
	 * Stores the current state of the game in a buffer. 
	 * @param sample the current state of the game.
	 */
	public void sample(double[] sample);	
}
