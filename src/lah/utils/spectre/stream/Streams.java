package lah.utils.spectre.stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lah.utils.spectre.BuildConfig;

/**
 * Various static (Input|Output)Stream utilities
 * 
 * @author L.A.H.
 * 
 */
public class Streams {

	/**
	 * Close {@link InputStream} while ignoring any exception raised
	 * 
	 * @param input_stream
	 *            Input stream to close
	 */
	public static void closeStream(InputStream input_stream) {
		try {
			input_stream.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Close {@link OutputStream} while ignoring any exception raised
	 * 
	 * @param out_stream
	 *            Output stream to close
	 */
	public static void closeStream(OutputStream out_stream) {
		try {
			out_stream.close();
		} catch (IOException e) {
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
	public static void pipeIOStream(InputStream input_stream,
			OutputStream output_stream) throws IOException,
			InterruptedException {
		if (input_stream == null)
			return;

		byte[] buffer = new byte[BuildConfig.BUFFER_SIZE];
		int count;
		while ((count = input_stream.read(buffer)) != -1) {
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException("Streaming is interrupted.");
			if (output_stream != null) {
				output_stream.write(buffer, 0, count);
				output_stream.flush();
			}
		}
	}

	/**
	 * Higher-order method to process a stream; note that the stream is not
	 * closed at the end of the processing! This method is blocking until the
	 * stream is fully processed or the calling thread is interrupted.
	 * 
	 * @param stream
	 * @param stream_processor
	 * @throws Exception
	 */
	public static void processStream(InputStream stream,
			IBufferProcessor stream_processor) throws Exception {
		if (stream == null)
			return;
		int count;
		byte[] buffer = new byte[BuildConfig.BUFFER_SIZE];
		if (stream_processor != null)
			stream_processor.reset();
		while ((count = stream.read(buffer)) != -1) {
			if (Thread.currentThread().isInterrupted())
				break;
			if (stream_processor != null)
				stream_processor.processBuffer(buffer, count);
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

	/**
	 * Stream an {@link InputStream} to a local file
	 * 
	 * @param inpstr
	 * @param out
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void streamToFile(InputStream inpstr, File out,
			boolean deleteOnInterruption, boolean append) throws IOException,
			InterruptedException {
		FileOutputStream out_str = new FileOutputStream(out, append);
		try {
			pipeIOStream(inpstr, out_str);
		} catch (InterruptedException e) {
			if (deleteOnInterruption)
				out.delete();
			throw e;
		} finally {
			out_str.close();
		}
	}

	/**
	 * Write or append a string to a file
	 * 
	 * @param content
	 *            string to write/append
	 * @param output
	 *            {@link File} to write or append
	 * @param append
	 *            {@literal true} to append the file instead of overwriting
	 * @throws IOException
	 */
	public static void writeStringToFile(String content, File output,
			boolean append) throws IOException {
		FileWriter writer = new FileWriter(output, append);
		writer.write(content);
		writer.close();
	}

}
