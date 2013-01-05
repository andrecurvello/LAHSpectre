package lah.spectre;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class functions as a general purpose network resource retrieval manager.
 * It manages list of download files, support download resume and many more.
 * 
 * @author L.A.H.
 * 
 */
public class SpectreGet {

	// public interface DownloadFileListener {
	//
	// void notifyDownloadComplete();
	//
	// void setProgress(int total_bytes_downloaded);
	//
	// void setRemoteContentLength(int remote_file_length);
	//
	// }

	/**
	 * Interface for object that receives notification from downloading service
	 */
	public interface Listener {

		void notifyComplete();

		void notifyContentLength(long remote_file_length);

		void notifyException(Exception e);

		void notifyProgress(int total_bytes_downloaded);

	}

	public static class MakeDirectoryException extends Exception {

		private static final long serialVersionUID = -1449931212835266740L;

		public MakeDirectoryException(String output_directory) {
			super(output_directory);
		}
	}

	/**
	 * Class containing the return value of
	 * {@link SpectreGet#get(Listener, String, String, String, boolean)}
	 * including a local file (possibly {@literal null}) and a status indicator
	 * to indicate whether the file is fetched or returned due to the fact that
	 * it already exists with the expected length returned from the remote host.
	 */
	public static class Result {

		boolean is_new;

		File output_file;

		Result(File output, boolean isnew) {
			output_file = output;
			is_new = isnew;
		}

		public File getOutputFile() {
			return output_file;
		}

		public boolean isNewFile() {
			return is_new;
		}

	}

	/**
	 * Prefix for a temporary file name
	 */
	private static final String TEMP_FILE_NAME = "temp.spectreget";

	/**
	 * Pick a valid file name for a temporary file in a directory.
	 * 
	 * @param directory
	 *            The directory in which we want to create a new temp file,
	 *            assuming to be an existing & readable directory
	 * @return A name for a new file in output_directory, this file name is of
	 *         the form {@link SpectreGet#TEMP_FILE_NAME} followed by a suffix
	 *         of form "(<integer>)" such as (0), (1), ...
	 */
	private static String createNewTemporaryFileName(String directory) {
		assert (directory != null);

		File dir = new File(directory);
		assert (dir.exists() && dir.canRead());

		// List the files starting with {@link SpectreGet#TEMP_FILE_NAME}
		String[] tmpfiles = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(TEMP_FILE_NAME);
			}
		});

		if (tmpfiles.length == 0)
			return TEMP_FILE_NAME + "(0)";

		// Select a new suffix
		Arrays.sort(tmpfiles);
		for (int i = 0;; i++) {
			String fname = TEMP_FILE_NAME + "(" + i + ")";
			if (Arrays.binarySearch(tmpfiles, fname) >= 0)
				return fname;
		}
	}

	/**
	 * Download file considering whether it exists locally or not.
	 * 
	 * @param uri
	 *            URI to download the file from
	 * @param output_directory
	 *            Path to the directory to write the file to
	 * @param output_file_name
	 *            Name of the file to write, {@literal null} to pick the file
	 *            name that the server give or a temporary file name
	 * @param force_overwrite
	 *            Download and overwrite the file if it exists at the location
	 * @return A {@link File} object representing the downloaded file or
	 *         {@literal null} if it cannot be downloaded or written to the
	 *         intended location
	 * 
	 * @throws IOException
	 */
	// public static File downloadFile(DownloadFileListener listener, String
	// uri,
	// String output_directory, String output_file_name,
	// boolean force_overwrite) {
	// // Note that only output stream needs to be closed properly.
	// // Input stream does not cause problem, after this method returns
	// // the data structure will be clean up.
	//
	// // Print out the download job summary
	// if (BuildConfig.DEBUG) {
	// System.out.println("TeX.downloadFile : URI = " + uri);
	// System.out.println("TeX.downloadFile : Output location = "
	// + output_directory);
	// System.out.println("TeX.downloadFile : Output file name = "
	// + (output_file_name == null ? "<unspecified>"
	// : output_file_name));
	// System.out.println("TeX.downloadFile : Force overwrite = "
	// + (force_overwrite ? "yes" : "no"));
	// }
	//
	// // Create the output directory if it does not exists
	// File dir = new File(output_directory);
	// if (!dir.exists() && dir.mkdirs()) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Error - output directory cannot be created.");
	// return null;
	// }
	//
	// // Now download the file if
	// // we are forced to overwrite (force_overwrite = true); or
	// // the output file does not exists; or
	// // the output file exists but its length does not match the length
	// // specified by the server.
	// URL url;
	// URLConnection urlconn;
	// try {
	// url = new URL(uri);
	// urlconn = url.openConnection();
	// urlconn.connect();
	// } catch (IOException e) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Error - cannot open the connection.");
	// return null;
	// }
	//
	// int remote_file_length = urlconn.getContentLength();
	// System.out.println("TeX.downloadFile : Remote file size = "
	// + remote_file_length + " bytes.");
	//
	// // Try to pick a name for output file
	// if (output_file_name == null) {
	// String disp = urlconn.getHeaderField("Content-Disposition");
	// if (disp != null) {
	// String[] params = disp.split(";");
	// for (int i = 0; i < params.length; i++) {
	// params[i] = params[i].trim();
	// // params[i] is of the form filename="...."
	// if (params[i].startsWith("filename=\"")) {
	// int s = "filename=\"".length();
	// int e = params[i].length() - 1;
	// output_file_name = params[i].substring(s, e);
	// if (BuildConfig.DEBUG) {
	// System.out
	// .println("TeX.downloadFile : Remote file name = "
	// + output_file_name
	// + " (obtained from response header, select it as the output file name)");
	// }
	// break;
	// }
	// }
	// }
	// if (output_file_name == null) {
	// output_file_name = createNewTemporaryFileName(output_directory);
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Name for output file = "
	// + output_file_name
	// + " (File name not found from header. Create a temporary value.)");
	// }
	// }
	//
	// // Now we have a legitimate file name, make the output file
	// File output = new File(output_directory + File.separator
	// + output_file_name);
	//
	// // Negation of download condition
	// if (!force_overwrite && output.exists()
	// && remote_file_length == output.length()) {
	// if (BuildConfig.DEBUG)
	// System.out.println("TeX.downloadFile : File " + output
	// + " already exists with matching content length"
	// + " and I am not forced to redownload,"
	// + " so I simply return it.");
	// return output;
	// }
	//
	// if (listener != null)
	// listener.setRemoteContentLength(remote_file_length);
	//
	// // Stream the remote file to local storage
	//
	// // Open the stream to read from remote host
	// // This should not be closed as closing might block. Simply let the
	// // method returns and the resource is automatically cleaned up
	// InputStream remote_content_input_stream = null;
	// try {
	// remote_content_input_stream = urlconn.getInputStream();
	// } catch (IOException e) {
	// if (BuildConfig.DEBUG)
	// System.out.println("TeX.downloadFile : Cannot stream " + uri);
	// e.printStackTrace();
	// return null;
	// }
	//
	// OutputStream download_file_output_stream = null;
	// try {
	// download_file_output_stream = new BufferedOutputStream(
	// new FileOutputStream(output));
	// } catch (FileNotFoundException e) {
	// if (BuildConfig.DEBUG)
	// System.out.println("TeX.downloadFile : File " + output
	// + " does not exists");
	// e.printStackTrace();
	// return null;
	// }
	//
	// byte[] buffer = new byte[1024];
	// int total_bytes_downloaded = 0;
	// int count;
	// do {
	// // Fetch 1KB from remote host to the buffer
	// try {
	// count = remote_content_input_stream.read(buffer);
	// if (count == -1)
	// break;
	// } catch (IOException e1) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Cannot read data from remote host.");
	// e1.printStackTrace();
	// // closeStream(download_file_output_stream);
	// try {
	// download_file_output_stream.close();
	// } catch (IOException e) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.closeInputStream : Error closing input stream "
	// + download_file_output_stream);
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// // Halt the download, delete partially downloaded file and
	// // return 'null' if we are interrupted
	// //
	// http://stackoverflow.com/questions/65035/in-java-does-return-trump-finally
	// if (Thread.currentThread().isInterrupted()) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Download is interrupted."
	// + " Removing partial download file.");
	// // closeStream(download_file_output_stream);
	// output.delete();
	// try {
	// download_file_output_stream.close();
	// } catch (IOException e) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.closeInputStream : Error closing input stream "
	// + download_file_output_stream);
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// // Write the data to the output stream
	// total_bytes_downloaded += count;
	// try {
	// download_file_output_stream.write(buffer, 0, count);
	// if (BuildConfig.DEBUG)
	// System.out.println("TeX.downloadFile : "
	// + total_bytes_downloaded
	// + "\t/"
	// + (remote_file_length == -1 ? "Unknown"
	// : remote_file_length));
	// if (listener != null)
	// listener.setProgress(total_bytes_downloaded);
	// } catch (IOException e) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Cannot write data to file "
	// + output);
	// // closeStream(download_file_output_stream);
	// e.printStackTrace();
	// try {
	// download_file_output_stream.close();
	// } catch (IOException e1) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.closeInputStream : Error closing input stream "
	// + download_file_output_stream);
	// e1.printStackTrace();
	// }
	// return null;
	// }
	// } while (true);
	//
	// if (BuildConfig.DEBUG)
	// System.out.println("TeX.downloadFile : Download finishes!");
	//
	// try {
	// download_file_output_stream.close();
	// } catch (IOException e) {
	// if (BuildConfig.DEBUG)
	// System.out
	// .println("TeX.downloadFile : Error closing file output stream!");
	// e.printStackTrace();
	// }
	//
	// if (listener != null)
	// listener.notifyDownloadComplete();
	//
	// return output;
	//
	// // try {
	// //
	// //
	// //
	// // } catch (IOException e) {
	// // if (BuildConfig.DEBUG) {
	// //
	// System.out.println("TeX.downloadFile : File cannot be written or download is interrupted!");
	// //
	// System.out.println("TeX.downloadFile : Delete the partially downloaded file.");
	// // }
	// // output.delete();
	// // e.printStackTrace();
	// // throw e;
	// // } finally {
	// // download_file_output_stream.close();
	// // remote_content_input_stream.close();
	// // }
	// }

	/**
	 * Get a file in background and inform the listener about the content-length
	 * (-1 if not known), the progress (in term of number of bytes downloaded)
	 * and the successfully retrieved or erroneous state.
	 * 
	 * @param listener
	 * @param uri
	 * @param output_directory
	 * @param output_file_name
	 * @param force_download
	 * @return
	 * @throws InterruptedException
	 *             if the thread running this method is interrupted
	 * @throws IOException
	 *             if there is I/O error such as cannot read from the remote
	 *             resource, cannot write file (denied access permission or
	 *             there is no more disk space), ...
	 * @throws MakeDirectoryException
	 *             if the output directory does not exist and it cannot be
	 *             created
	 * @throws NullPointerException
	 *             if any of the URI or output directory is null
	 */
	public static Result get(Listener listener, String uri,
			String output_directory, String output_file_name,
			boolean force_download) throws InterruptedException,
			MakeDirectoryException, IOException {
		// Print a summary of what we are going to do
		summarize(listener, uri, output_directory, output_file_name,
				force_download);

		// Create the output directory if it does not exists
		File dir = new File(output_directory);
		if (!dir.exists()) {
			dir.mkdirs();
			if (!dir.exists())
				throw new MakeDirectoryException("Cannot create directory "
						+ output_directory + " to write downloaded file to.");
		}

		if (BuildConfig.DEBUG)
			System.out.println("SpectreGet.get : Openning connection.");
		URLConnection urlconn;
		try {
			// Open a connection
			URL url = new URL(uri);
			urlconn = url.openConnection();
			if (BuildConfig.DEBUG)
				System.out
						.println("SpectreGet.get : Connection is open. Now we try to connect.");
			urlconn.connect();
			if (BuildConfig.DEBUG)
				System.out.println("SpectreGet.get : We are connected.");
		} catch (IOException e) {
			// possibly a malform URL as well!
			throw new IOException(
					"Cannot open connection. Please check your network.");
		}

		// Determine the content length
		if (BuildConfig.DEBUG)
			System.out
					.println("SpectreGet.get : Try to get the content length.");
		// getContentLengthLong() is preferred but it is ONLY available at 1.7!
		int remote_file_length = urlconn.getContentLength();
		if (BuildConfig.DEBUG)
			System.out.println("SpectreGet.get : Remote content length = "
					+ remote_file_length);

		// Now download the file if
		// we are forced to download; or
		// the output file does not exists; or
		// the output file exists but its size does not match the content length
		// specified by the server.
		String disp = urlconn.getHeaderField("Content-Disposition");
		if (BuildConfig.DEBUG)
			System.out.println("SpectreGet.get : Content-Disposition := "
					+ disp);
		output_file_name = getOutputFileName(output_file_name, disp,
				output_directory);
		File output = new File(output_directory + File.separator
				+ output_file_name);
		if (BuildConfig.DEBUG)
			System.out.println("SpectreGet.get : Finalized output file := "
					+ output);

		if (!force_download && output.exists()
				&& remote_file_length == output.length()) {
			// Negation of download condition: simply return the existing file
			return new Result(output, false);
		}

		if (listener != null)
			listener.notifyContentLength(remote_file_length);

		InputStream remote_input_stream;
		try {
			remote_input_stream = urlconn.getInputStream();
			OutputStream file_output_stream = new BufferedOutputStream(
					new FileOutputStream(output));

			byte[] buffer = new byte[BuildConfig.BUFFER_SIZE];
			int total_num_bytes_downloaded = 0;
			int count;

			// Notify the listener that the download now starts
			if (listener != null)
				listener.notifyProgress(total_num_bytes_downloaded);

			// Fetch data from remote host to the buffer
			while (true) {
				try {
					count = remote_input_stream.read(buffer);
					if (count == -1)
						break;
				} catch (IOException e) {
					// Catch the exception to close the stream and then throw it
					// back to the caller to handle.
					file_output_stream.close();
					throw e;
				}

				// If we are interrupted after reading (but not finish writing)
				// throw an InterruptedException to the caller
				if (Thread.currentThread().isInterrupted()) {
					file_output_stream.close();
					throw new InterruptedException(
							"Interrupted while downloading file.");
				}

				// Write the data to the output stream
				total_num_bytes_downloaded += count;
				file_output_stream.write(buffer, 0, count);
				if (listener != null)
					listener.notifyProgress(total_num_bytes_downloaded);
			}

			// Remark: we only close the (local) output stream, the remote
			// stream
			// should not be closed since it might cause block.
			file_output_stream.close();

			if (listener != null)
				listener.notifyComplete();

			return new Result(output, true);
		} catch (IOException e) {
			throw new IOException(
					"Cannot download or write file! This is possibly because:"
							+ "The remote host is down.\n"
							+ "You have no Internet connection.\n"
							+ "You are disconnected.\n"
							+ "Your device is write-protected or out of space.");
		}
	}

	/**
	 * Get a valid output file name for the output
	 * 
	 * @param output_file_name
	 * @param disp
	 * @param output_directory
	 * @return
	 */
	private static String getOutputFileName(String output_file_name,
			String disp, String output_directory) {
		if (output_file_name != null)
			return output_file_name;

		if (disp != null) {
			// Try to pick the name from the content-disposition field of the
			// response header
			String[] params = disp.split(";");
			for (int i = 0; i < params.length; i++) {
				params[i] = params[i].trim();
				if (params[i].startsWith("filename=\"")) {
					// params[i] is of the form filename="<string>" extract
					// <string> from it.
					int s = "filename=\"".length();
					int e = params[i].length() - 1;
					output_file_name = params[i].substring(s, e);
					return output_file_name;
				}
			}
		}

		return createNewTemporaryFileName(output_directory);
	}

	/**
	 * Get an {@link InputStream} to read information from a network resource
	 * from the URI
	 * 
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public static InputStream streamFile(String uri) throws IOException {
		URL url = new URL(uri);
		URLConnection urlconn = url.openConnection();
		urlconn.connect();
		return urlconn.getInputStream();
	}

	private static void summarize(Listener listener, String uri,
			String output_directory, String output_file_name,
			boolean force_overwrite) {
		// Print out the download job summary
		if (BuildConfig.DEBUG) {
			System.out.println("SpectreGet.get : URI              := " + uri);
			System.out.println("SpectreGet.get : Output location  := "
					+ output_directory);
			System.out.println("SpectreGet.get : Output file name := "
					+ (output_file_name == null ? "<unspecified>"
							: output_file_name));
			System.out.println("SpectreGet.get : Force overwrite  := "
					+ (force_overwrite ? "yes" : "no"));
		}
	}

	private Future<?> current_get_task;

	/**
	 * {@link ExecutorService} to send download tasks to
	 */
	private ExecutorService task_scheduler;

	/**
	 * Create a new instance
	 */
	public SpectreGet() {
		task_scheduler = Executors.newSingleThreadExecutor();
	}

	/**
	 * Create a new instance with a specified executor
	 */
	public SpectreGet(ExecutorService executor) {
		task_scheduler = executor;
	}

	/**
	 * Cancel the
	 * {@link SpectreGet#getInBackground(Listener, String, String, String, boolean)}
	 * operation submitted.
	 */
	public void cancelBackgroundGet() {
		if (current_get_task != null)
			current_get_task.cancel(true);
	}

	public void getInBackground(final Listener listener, final String uri,
			final String output_directory, final String output_file_name,
			final boolean force_overwrite) {
		current_get_task = task_scheduler.submit(new Runnable() {

			@Override
			public void run() {
				try {
					get(listener, uri, output_directory, output_file_name,
							force_overwrite);
				} catch (Exception e) {
					e.printStackTrace();
					listener.notifyException(e);
				} finally {
					current_get_task = null;
				}
			}
		});
	}

}