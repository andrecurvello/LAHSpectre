package lah.utils.spectre.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class StreamRedirector implements InputBufferProcessor {

	public static final InputBufferProcessor NULL = new InputBufferProcessor() {
		public void processBuffer(byte[] buffer, int count) throws Exception {
			// Do nothing, just ignore the output
		}

		public void reset() {
		}
	};

	public static final InputBufferProcessor STDOUT = new InputBufferProcessor() {
		public void processBuffer(byte[] buffer, int count) throws Exception {
			System.out.println(new String(buffer, 0, count));
		}

		public void reset() {
		}
	};

	private OutputStream out_stream;

	public StreamRedirector(File output_file, boolean append)
			throws FileNotFoundException {
		this(new FileOutputStream(output_file, append));
	}

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
