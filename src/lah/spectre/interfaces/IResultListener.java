package lah.spectre.interfaces;

/**
 * Interface for listener awaiting for a result
 * 
 * @author L.A.H.
 * 
 * @param <R>
 */
public interface IResultListener<R> {

	/**
	 * Invoke by the computing object when the result is fully obtained
	 * 
	 * @param result
	 *            The result
	 */
	void onResultObtained(R result);

}
