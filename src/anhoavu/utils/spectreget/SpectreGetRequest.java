package anhoavu.utils.spectreget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link Runnable} that do the file downloading to be created at
 * {@link SpectreGet#requestResource(SpectreGetListener, String, String)}
 * This object does only one thing: download and notify the listeners.
 * Object of this class should not be created anywhere else.
 * 
 * @author Vu An Hoa
 * 
 */
public class SpectreGetRequest implements Runnable {

	private final int BUFFER_SIZE = 4096;

	/**
	 * Identifier of this object
	 */
	private int request_id;

	/**
	 * The URI of the resource
	 */
	private String resource_uri;

	/**
	 * The path to the retrieved output file (including the file name)
	 */
	private String resource_output_location;

	/**
	 * The object interested in the resource.
	 */
	private SpectreGetListener resource_listener;

	/**
	 * The {@link SpectreGet} object initiated this resource request
	 */
	private SpectreGet request_coordinator;

	public SpectreGetRequest(SpectreGet coordinator, int reqid,
			SpectreGetListener listener, String uri, String output_loc) {
		request_coordinator = coordinator;
		request_id = reqid;
		resource_uri = uri;
		resource_output_location = output_loc;
		resource_listener = listener;
	}

	/**
	 * Retrieve the resource
	 */
	public void run() {
		// System.out.println("Download from " + resource_uri + " and write to "
		// + resource_output_location);
		File f = new File(resource_output_location);

		if (f.exists()) {
			if (f.isDirectory()) {
				String error_msg = "Error: " + resource_output_location
						+ " is an existing directory!";
				Exception e = new Exception(error_msg);
				if (request_coordinator != null)
					request_coordinator.onRequestException(request_id, e);
				if (resource_listener != null)
					resource_listener.onException(request_id, e);
			} else {
				System.out.println("Warning: " + resource_output_location
						+ " already exists!");
				// TODO Add overwrite-on-existing file option
				// or call-back-resume on existing file
				System.out.println("Returning the existing file"
						+ " (instead of download & overwrite).");
				if (request_coordinator != null)
					request_coordinator.onRequestCompleted(request_id);
				if (resource_listener != null)
					resource_listener.onResourceComplete(request_id, f);
			}
		}

		// the file does not exist yet
		URL url;
		try {
			url = new URL(resource_uri);
			URLConnection urlconn = url.openConnection();
			urlconn.connect();
			if (resource_listener != null)
				resource_listener.onResourceContentLength(request_id,
						urlconn.getContentLength());
			InputStream is = urlconn.getInputStream();

			// create the necessary containing directories if they do not exists
			if (f.getParent() != null)
				f.getParentFile().mkdirs();

			// stream the remote file to local storage
			FileOutputStream fos = new FileOutputStream(f);
			byte[] buffer = new byte[BUFFER_SIZE];
			int total_byte_read_so_far = 0;
			int count;
			while ((count = is.read(buffer)) != -1) {
				total_byte_read_so_far += count;
				fos.write(buffer, 0, count);
				if (resource_listener != null)
					resource_listener.onProgressUpdate(request_id,
							total_byte_read_so_far);
			}
			fos.close();
			is.close();

			// System.out.println("Download finish!");
			if (request_coordinator != null)
				request_coordinator.onRequestCompleted(request_id);
			if (resource_listener != null)
				resource_listener.onResourceComplete(request_id, f);
		} catch (Exception e) {
			// 3 possible exceptions: MalformedURLException,
			// FileNotFoundException, IOException
			// We will imply pass it back to the coordinator and inform the
			// listener accordingly
			if (request_coordinator != null)
				request_coordinator.onRequestException(request_id, e);
			if (resource_listener != null)
				resource_listener.onException(request_id, e);
		}
	}

}
