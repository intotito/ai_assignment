package ie.atu.sw.model;

public interface Trainer {
	public void startRecording();
	public void stopRecording();
	public void record(double[] sample);
}
