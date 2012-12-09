package lah.utils.spectre.interfaces;

/**
 * Interface for general object that consumes resources produced by
 * {@link ResourceSupplier}.
 * 
 * @author Vu An Hoa
 * 
 * @param <O>
 *            Type of result
 * @param <P>
 *            Type of progress report
 */
public interface ResourceListener<O, P> {

	void onResourceProgress(P progress);

	void onResourceReceived(O resource);

}
