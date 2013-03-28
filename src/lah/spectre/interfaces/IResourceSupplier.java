package lah.spectre.interfaces;

/**
 * Interface for objects that produces resources and call the corresponding
 * listener when it is available for processing.
 * 
 * @author L.A.H.
 * 
 * @param <K>
 *            Type of key
 * @param <R>
 *            Type of resource
 */
public interface IResourceSupplier<K, R> {

	/**
	 * Interface for general object that consumes resources produced by
	 * {@link IResourceSupplier}.
	 * 
	 * @author L.A.H.
	 * 
	 * @param <P>
	 *            Type of progress report
	 * @param <R>
	 *            Type of resource
	 */
	public static interface Listener<P, R> {

		void onComplete(R resource);

		void onProgress(P progress);

	}

	/**
	 * Get the resource with the supplied key
	 * 
	 * @param key
	 *            Key of the resource
	 */
	R getResource(K key) throws Exception;

}
