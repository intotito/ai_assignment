package ie.atu.sw.model;
/**
 * This interface specifies the functionality required to write sample to file.
 */
public interface Trainer {
	/**
	 * Classes implementing this method is expected to open connections to I/O resources
	 * and do nothing if the resource are already opened. 
	 */
	public void startRecording();
	/**
	 * Classes implementing this method is expected close all open I/O resources 
	 * if open or do nothing if not opened.
	 */
	public void stopRecording();
}
