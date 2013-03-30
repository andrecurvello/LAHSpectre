package lah.spectre;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import lah.spectre.stream.Streams;

/**
 * This class functions as a general purpose network resource retrieval manager.
 * It manages list of download files, support download resume and many more.
 * 
 * @author L.A.H.
 * 
 */
public class SpectreGet {

	/**
	 * Download a file
	 * 
	 * @param uri
	 *            URI of the file to download
	 * @param output_file
	 *            The expected downloaded File
	 * @return
	 * @throws Exception
	 */
	public static File downloadToFile(String uri, File output_file) throws Exception {
		if (!output_file.getParentFile().exists())
			output_file.getParentFile().mkdirs();
		try {
			URL url = new URL(uri);
			URLConnection urlconn = url.openConnection();
			urlconn.connect();
			Streams.streamToFile(urlconn.getInputStream(), output_file, true, false);
			return output_file.exists() ? output_file : null;
		} catch (Exception e) {
			output_file.delete(); // for safety, delete the file on failure
			throw e;
		}
	}

}