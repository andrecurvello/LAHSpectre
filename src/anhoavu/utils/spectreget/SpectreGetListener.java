package anhoavu.utils.spectreget;

import java.io.File;

/**
 * This interface allows caller (user interface) to get notified by
 * {@link SpectreGetRequest} on events such as
 * <ol>
 * <li>download complete</li>
 * <li>download cancel (on error)</li>
 * <li>download progress (number of byte received)</li>
 * </ol>
 * 
 * @author Vu An Hoa
 * 
 */
public interface SpectreGetListener {

	/**
	 * Call back function in case the resource is not available
	 */
	void onException(int request_id, Exception e);

	/**
	 * Call back function in case the resource is completely obtain
	 * 
	 * @param f
	 */
	void onResourceComplete(int request_id, File f);

	/**
	 * The retrieval progress, use {@link SpectreGet#getMaxProgress} to obtain
	 * the maximum progress
	 * 
	 * @param progress
	 *            The progress
	 */
	void onProgressUpdate(int request_id, int progress);

	/**
	 * Call when the {@link SpectreGetRequest} decode the content length
	 * of the web request
	 * 
	 * @param contentLength
	 */
	void onResourceContentLength(int request_id, int contentLength);

}
