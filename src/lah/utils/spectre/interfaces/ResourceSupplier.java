package lah.utils.spectre.interfaces;

/**
 * Interface for objects that produces resources and call the corresponding
 * listener when it is available for processing.
 * 
 * @author L.A.H.
 * 
 * @param <I>
 *            Type of input
 * @param <E>
 *            Type of output
 */
public interface ResourceSupplier<I, O> {

	/**
	 * Compute the requested resource
	 * 
	 * @param args
	 * @return
	 */
	O getResource(I inputs);

}
