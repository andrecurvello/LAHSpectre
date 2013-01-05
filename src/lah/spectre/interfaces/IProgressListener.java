package lah.spectre.interfaces;

/**
 * Interface for object listening to the progress of a computational process
 * 
 * @author L.A.H.
 * 
 * @param <P>
 *            Type of the progress encapsulating object
 */
public interface IProgressListener<P> {

	/**
	 * Invoke when the computational process wants to update its progress
	 * 
	 * @param progress
	 *            The new progress checkpoint
	 */
	void onProgress(P progress);

}
