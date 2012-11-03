package lah.utils.spectre;

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

	void onResourceReceived(O resource);

	void onResourceProgress(P progress);

}
