package ie.atu.sw.model;

public abstract class Agent implements AutoPilot{
	protected int width, height, STAGE_WIDTH, STAGE_HEIGHT;
	private int pointer = 0;
	private double[] buffer;
	private int PLAYER_COLUMN;
	private boolean willSample = false;
	
	public Agent(int STAGE_WIDTH, int STAGE_HEIGHT, int PLAYER_COLUMN, int horizon) {
		this.PLAYER_COLUMN = PLAYER_COLUMN;
		this.STAGE_WIDTH = STAGE_WIDTH;
		this.STAGE_HEIGHT = STAGE_HEIGHT;
		this.width = horizon;
		this.height = 2 * horizon + 1;
		buffer = new double[(STAGE_WIDTH - PLAYER_COLUMN - 1) * STAGE_HEIGHT];
	}

	public double[] extractSample(int player_row) {
		double[] value = new double[width * height];
		int indexTop = player_row - width;
		if(STAGE_HEIGHT - player_row - 1 < width) {
			indexTop = STAGE_HEIGHT - height;
		} else if(indexTop < 0) {
			indexTop = 0;
		}
		for(int i = 0; i < width; i++) {
			System.arraycopy(buffer, (pointer + i) * STAGE_HEIGHT + indexTop, value, i * height, height);
		}
		pointer++;
		willSample = pointer >= (STAGE_WIDTH - PLAYER_COLUMN - 1 - width);
		return value;
	}
	@Override
	public boolean look() {
		return willSample;
	}
	
	@Override
	public void sample(double[] sample) {
		System.arraycopy(sample, (PLAYER_COLUMN + 1) * STAGE_HEIGHT, buffer, 0, buffer.length);
		pointer = 0;
		willSample = false;
	}

	public void decipher(double[] sample, int cols, int height) {
		for (int i = 0; i < cols * height; i++) {
			int y = (height * i) - (i / cols) * (cols * height - 1);
			System.out.printf("%d%c", (int) sample[y], (((i + 1) % (cols) != 0) || i == 0 && cols != 1) ? ',' : '\n');
		}
		System.out.println("-----------------------------------------------------------");
	}

	
}
