package anhoavu.utils.spectreget;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class functions as a general purpose network resource retrieval manager.
 * It manages list of download files, support download resume and many more.
 * 
 * @author Vu An Hoa
 * 
 */
public class SpectreGet {

	/**
	 * This {@link SparseArray} map an integer ID to the Future object that
	 * perform the actual resource retrieval. This mapping is used to cancel a
	 * resource request on demand.
	 */
	private Map<Integer,Future<?>> request_futures;

	/**
	 * This {@link BitSet} remember used request ID, necessary to get the first
	 * unused ID to assign to new requests
	 */
	private BitSet used_request_ids;

	/**
	 * This {@link ExecutorService} is in charge of executing requests
	 */
	private ExecutorService exec_service;

	private static SpectreGet uniq_spectre;

	private SpectreGet() {
		request_futures = new TreeMap<Integer,Future<?>>();
		used_request_ids = new BitSet();
		exec_service = Executors.newFixedThreadPool(2);
	}

	/**
	 * Get the unique instance of {@link SpectreGet}
	 * 
	 * @return
	 */
	public SpectreGet getInstance() {
		// restart periodically
		if (uniq_spectre == null)
			uniq_spectre = new SpectreGet();
		return uniq_spectre;
	}

	/**
	 * Request for resource.
	 * 
	 * @param listener
	 *            The resource requester, set to 'null' if you are not
	 *            interested in the result (this has the same effect as an
	 *            asynchronous download request.) The interesting thing is that
	 *            the caller can delegate the listening and reacting job to
	 *            someone else!
	 * @param uri
	 *            The URI to the resource
	 * @param output_loc
	 *            The output file to dump the resource
	 * @return An integer ID assigned to this request, use this ID to cancel the
	 *         request if you want to or to match the request with the updates
	 *         from the {@link SpectreGetRequest} object that will handle the
	 *         actual request.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public int requestResource(SpectreGetListener listener, String uri,
			String output_loc) throws InterruptedException, ExecutionException {
		// create a new ID for the request
		int reqid = used_request_ids.nextClearBit(0);
		used_request_ids.set(reqid);

		// execute the request
		SpectreGetRequest request = new SpectreGetRequest(this, reqid,
				listener, uri, output_loc);
		Future<?> request_future = exec_service.submit(request);

		// remember the request in order to
		// cancel them later should it be necessary
		request_futures.put(reqid, request_future);

		return reqid;
	}

	/**
	 * Allow a caller to cancel an existing request.
	 * 
	 * Warning: there is security issue here - this allows an object to cancel
	 * another object's request.
	 * 
	 * @param request_id
	 *            The integer ID assigned to the request at the time of calling
	 *            to {@link SpectreGet#requestResource}
	 * @param reason
	 *            Reason of the cancellation, for log-in purpose
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void cancelResourceRequest(int request_id, String reason) {
		Future<?> request_future = request_futures.get(request_id);
		if (request_future != null) {
			request_future.cancel(true);
			request_futures.remove(request_id);
			used_request_ids.clear(request_id);
		}
	}

	/**
	 * Cancel the resource request due to an exception.
	 * 
	 * Warning: This method should only be called by the
	 * {@link SpectreGetRequest} and is therefore not made public.
	 * 
	 * @param request_id
	 * @param e
	 */
	void onRequestException(int request_id, Exception e) {
		cancelResourceRequest(request_id, e.getMessage());
	}

	/**
	 * Call by {@link SpectreGetRequest} when the request is already completed,
	 * this is necessary for us to remove reuse the ID pool
	 * 
	 * Warning: This method should only be called by the
	 * {@link SpectreGetRequest} and is therefore not made public.
	 * 
	 * @param request_id
	 */
	void onRequestCompleted(int request_id) {
		request_futures.remove(request_id);
		used_request_ids.clear(request_id);
	}

}