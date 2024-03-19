package ie.atu.sw.model;
/**
 * This class is an abstract implementation of the {@link ie.atu.sw.model.AutoPilot} interface. It provides
 * the functionality for buffering and initialization of necessary properties. 
 */
public abstract class Agent implements AutoPilot{
	/**
	 * The width of ship horizon
	 */
	private int width;
	/**
	 * The height of the ship horizon
	 */
	private int height;
	/**
	 * Width of the Stage
	 */
	private int stageWidth;
	/**
	 * Height of the Stage
	 */
	private int stageHeight;
	/**
	 * A pointer indicating the current location in the buffer
	 */
	private int pointer = 0;
	/**
	 * A Buffer where the state of the Stage is saved
	 */
	private double[] buffer;
	/**
	 * The column the ship is occupying on the Stage. 
	 */
	private int playerColumn;
	/**
	 * Determines if new will be copied to the buffer. 
	 */
	private boolean willSample = false;
	
	/**
	 * This constructor takes in four integer parameters required to initialize the Agent. 
	 * @param stageWidth the width of the stage
	 * @param stageHeight the height of the stage
	 * @param playerColumn the column the ship will be occupying in the stage
	 * @param horizon the longitudinal horizon of the ship
	 */
	public Agent(int stageWidth, int stageHeight, int playerColumn, int horizon) {
		this.playerColumn = playerColumn;
		this.stageWidth = stageWidth;
		this.stageHeight = stageHeight;
		this.width = horizon;
		this.height = 2 * horizon + 1;
		buffer = new double[(stageWidth - playerColumn - 1) * stageHeight];
	}

	/**
	 * This method extracts a sample grid of (width) * (2 * width + 1) with respect to the ships 
	 * current location from the buffer. This method is guaranteed to return a sample of the said size irrespective
	 * of the ship location with respect to the Stage.
	 * @param player_row the row the ship is currently occupying
	 * @return a sample of the Stage state
	 */
	
	public double[] extractSample(int player_row) {
		double[] value = new double[width * height];
		int indexTop = player_row - width;
		if(stageHeight - player_row - 1 < width) {
			indexTop = stageHeight - height;
		} else if(indexTop < 0) {
			indexTop = 0;
		}
		for(int i = 0; i < width; i++) {
			System.arraycopy(buffer, (pointer + i) * stageHeight + indexTop, value, i * height, height);
		}
		pointer++;
		willSample = pointer >= (stageWidth - playerColumn - 1 - width);
		return value;
	}
	/**
	 * This method is called every frame to check if the buffer is due for refilling. 
	 * @return true if buffer is empty and false otherwise
	 */
	public boolean look() {
		return willSample;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sample(double[] sample) {
		System.arraycopy(sample, (playerColumn + 1) * stageHeight, buffer, 0, buffer.length);
		pointer = 0;
		willSample = false;
	}

	/**
	 * A utility method that prints a given sample in a human decipherable way.
	 * @param sample the sample of data to print
	 * @param cols the column of the output
	 * @param height the height of the output
	 */
	protected void decipher(double[] sample, int cols, int height) {
		for (int i = 0; i < cols * height; i++) {
			int y = (height * i) - (i / cols) * (cols * height - 1);
			System.out.printf("%d%c", (int) sample[y], (((i + 1) % (cols) != 0) || i == 0 && cols != 1) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}
	/**
	 * Accessor method for {@link ie.sw.atu.model.Agent#width}
	 * @return the width of the ship's horizon
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Accessor method for {@link ie.sw.atu.model.Agent#height}
	 * @return the height of the ship's horizon
	 */
	public int getHeight() {
		return height;
	}

	
}
