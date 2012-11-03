package lah.utils.spectre;

/**
 * Interface for objects that produces resources and call the corresponding
 * listener when it is available for processing.
 * 
 * @author Vu An Hoa
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
