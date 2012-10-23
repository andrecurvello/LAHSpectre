package anhoavu.utils.spectre;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {

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
			if (BuildConfig.DEBUG)
				System.out
						.println("TeX.closeInputStream : Error closing input stream "
								+ inp_stream);
			e.printStackTrace();
		}
	}

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
			if (BuildConfig.DEBUG)
				System.out
						.println("TeX.closeInputStream : Error closing input stream "
								+ out_stream);
			e.printStackTrace();
		}
	}

	/**
	 * Pipe an input stream directly into an output stream; this is useful for
	 * various I/O purposes. The caller is in charge of time out this method to
	 * account for blocking input.
	 * 
	 * @param inpstr
	 *            {@link InputStream} to take from
	 * @param outstr
	 *            {@link OutputStream} to write to
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void pipeIOStream(InputStream inpstr, OutputStream outstr)
			throws IOException, InterruptedException {
		byte[] buffer = new byte[BuildConfig.BUFFER_SIZE];
		int count;

		while ((count = inpstr.read(buffer)) != -1) {
			// Make this reading & writing interrupt-safe
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException("Streams.pipeIOStream");

			outstr.write(buffer, 0, count);
		}
	}

	/**
	 * Read the content of a text file and return it as a string
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readTextFile(File file) throws IOException {
		BufferedInputStream file_inpstr = new BufferedInputStream(
				new FileInputStream(file));
		String content = readTillEnd(file_inpstr);
		file_inpstr.close();
		return content;
	}

	/**
	 * Read a text file to a string given its name
	 * 
	 * @param path_to_file
	 * @return A {@link String} containing the content of the file
	 * @throws FileNotFoundException
	 *             if the file does not exist
	 * @throws IOException
	 *             if the file cannot be read (for example, access denied)
	 */
	public static String readTextFile(String path_to_file)
			throws FileNotFoundException, IOException {
		File file = new File(path_to_file);
		return readTextFile(file);
	}

	/**
	 * Read an {@link InputStream} until the end into a {@link String}, return
	 * the partially read content if the running thread is interrupted
	 * 
	 * @param inpstr
	 *            {@link InputStream} to read from
	 * @return the {@link String} containing all bytes read from the stream till
	 *         end or right before interruption
	 * @throws IOException
	 *             if the stream cannot be read
	 */
	public static String readTillEnd(InputStream inpstr) throws IOException {
		byte[] buffer = new byte[BuildConfig.BUFFER_SIZE];
		StringBuilder stream_content_builder = new StringBuilder();
		int count;
		while (!Thread.currentThread().isInterrupted()
				&& (count = inpstr.read(buffer)) != -1) {
			stream_content_builder.append(new String(buffer, 0, count));
		}
		return stream_content_builder.toString();
	}

}
