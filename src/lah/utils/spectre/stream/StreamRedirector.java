package lah.utils.spectre.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Process a byte[] buffer by redirect it to an output stream
 * 
 * @author L.A.H.
 * 
 */
public class StreamRedirector implements IBufferProcessor {

	/**
	 * An {@link IBufferProcessor} that simply ignore the buffer
	 */
	public static final IBufferProcessor NULL = new IBufferProcessor() {
		public void processBuffer(byte[] buffer, int count) throws Exception {
			// Do nothing, just ignore the output
		}

		public void reset() {
		}
	};

	/**
	 * An {@link IBufferProcessor} that simply write the buffer to standard
	 * output
	 */
	public static final IBufferProcessor STDOUT = new IBufferProcessor() {
		public void processBuffer(byte[] buffer, int count) throws Exception {
			System.out.println(new String(buffer, 0, count));
		}

		public void reset() {
		}
	};

	/**
	 * Output stream to redirect the buffer to
	 */
	private OutputStream out_stream;

	/**
	 * Construct an instance that redirect the result to a file
	 * 
	 * @param output_file
	 * @param append
	 * @throws FileNotFoundException
	 */
	public StreamRedirector(File output_file, boolean append)
			throws FileNotFoundException {
		this(new FileOutputStream(output_file, append));
	}

	/**
	 * Construct an instance that redirect buffer to a specified stream
	 * 
	 * @param output_stream
	 */
	public StreamRedirector(OutputStream output_stream) {
		out_stream = output_stream;
	}

	@Override
	public void processBuffer(byte[] buffer, int count) throws Exception {
		if (out_stream != null) {
			out_stream.write(buffer, 0, count);
			out_stream.flush();
		}
	}

	@Override
	public void reset() {
		// Do nothing
	}

}
