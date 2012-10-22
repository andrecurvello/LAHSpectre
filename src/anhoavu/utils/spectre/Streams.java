package anhoavu.utils.spectre;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
	private static boolean DEBUG = true;

	/**
	 * Close stream without throwing exception.
	 * 
	 * @param inp_stream
	 *            Input stream to close
	 */
	public static void closeStream(InputStream inp_stream) {
		try {
			inp_stream.close();
		} catch (IOException e) {
			if (DEBUG)
				System.out
						.println("TeX.closeInputStream : Error closing input stream "
								+ inp_stream);
			e.printStackTrace();
		}
	}

	// static File downloadFile(String uri, String output_file) throws
	// IOException {
	// System.out.println("TeX.downloadFile : Download from " + uri
	// + " and write to " + output_file);
	//
	// File f = new File(output_file);
	//
	// if (f.exists()) {
	// if (f.isDirectory()) {
	// if (DEBUG)
	// System.out.println("TeX.downloadFile : Error - "
	// + output_file + " is a directory!");
	// return null;
	// } else {
	// if (DEBUG) {
	// System.out.println("TeX.downloadFile : Warning "
	// + output_file + " already exists!");
	// System.out
	// .println("TeX.downloadFile : Checking for size.");
	// }
	//
	// URL url = new URL(uri);
	// URLConnection urlconn = url.openConnection();
	// urlconn.connect();
	// int remote_file_length = urlconn.getContentLength();
	//
	// if (f.length() == remote_file_length) {
	// if (DEBUG)
	// System.out
	// .println("TeX.downloadFile : The size of local file and remote file matches. Returning the local file instead of redownload.");
	// // TODO Add overwrite-on-existing file option
	// // or call-back-resume on existing file
	// return f;
	// }
	// }
	// }
	//
	// // the file does not exist yet
	// URL url = new URL(uri);
	// URLConnection urlconn = url.openConnection();
	// urlconn.connect();
	// InputStream is = urlconn.getInputStream();
	//
	// // create the necessary containing directories for the target output
	// // file if they do not exists
	// if (f.getParent() != null)
	// f.getParentFile().mkdirs();
	//
	// // stream the remote file to local storage
	// FileOutputStream fos = new FileOutputStream(f);
	// pipeIOStream(is, fos);
	// fos.close();
	//
	// if (DEBUG)
	// System.out.println("TeX.downloadFile : Download finish!");
	// return f;
	// }

	/**
	 * Close stream without throwing exception.
	 * 
	 * @param out_stream
	 *            Output stream to close
	 */
	public static void closeStream(OutputStream out_stream) {
		try {
			out_stream.close();
		} catch (IOException e) {
			if (DEBUG)
				System.out
						.println("TeX.closeInputStream : Error closing input stream "
								+ out_stream);
			e.printStackTrace();
		}
	}

	/**
	 * Pipe an input stream directly into an output stream; this is useful for
	 * various I/O purposes.
	 * 
	 * @param inpstr
	 *            {@link InputStream} to take from
	 * @param outstr
	 *            {@link OutputStream} to write to
	 * @throws IOException
	 */
	public static void pipeIOStream(InputStream inpstr, OutputStream outstr)
			throws IOException {
		byte[] buffer = new byte[1024];
		int count;
		// Make this writing interrupt-safe
		// TODO what about blocking case?
		while (!Thread.currentThread().isInterrupted()
				&& (count = inpstr.read(buffer)) != -1)
			outstr.write(buffer, 0, count);
	}

	/**
	 * Read an {@link InputStream} until the end into a {@link String}
	 * 
	 * @param inpstr
	 *            {@link InputStream} to read from
	 * @return the {@link String} obtaining from all bytes read
	 * @throws IOException
	 */
	public static String readTillEnd(InputStream inpstr) throws IOException {
		byte[] buffer = new byte[1024];
		StringBuffer res = new StringBuffer();
		int count;
		while ((count = inpstr.read(buffer)) != -1)
			res.append(new String(buffer, 0, count));
		return res.toString();
	}

}
