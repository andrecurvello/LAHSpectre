package lah.spectre.interfaces;

/**
 * Interface for general object that consumes resources produced by
 * {@link IResourceSupplier}.
 * 
 * @author L.A.H.
 * 
 * @param <O>
 *            Type of result
 * @param <P>
 *            Type of progress report
 */
public interface IResourceListener<O, P> {

	void onResourceProgress(P progress);

	void onResourceReceived(O resource);

}
