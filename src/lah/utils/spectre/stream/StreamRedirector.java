package lah.utils.spectre.stream;

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

	private OutputStream output_stream;

	public StreamRedirector(OutputStream output_stream) {
		this.output_stream = output_stream;
	}

	@Override
	public void processBuffer(byte[] buffer, int count) throws Exception {
		if (output_stream != null)
			output_stream.write(buffer, 0, count);
	}

	@Override
	public void reset() {
		// Do nothing
	}

}
