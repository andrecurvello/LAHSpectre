package lah.utils.spectre.interfaces;


/**
 * This object
 * 
 * @author L.A.H.
 * 
 * @param <I>
 *            Type of input
 * @param <O>
 *            Type of output
 * @param <P>
 *            Type of progress report
 */
public interface ICallbackResourceSupplier<I, O, P> {

	/**
	 * Start obtaining the resource, post the progress and notify the final
	 * result to the listener object
	 * 
	 * @param args
	 * @param listener
	 */
	void getResource(I args, ResourceListener<O, P> listener);

}
