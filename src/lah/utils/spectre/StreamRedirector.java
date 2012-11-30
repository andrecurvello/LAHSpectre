package lah.utils.spectre;

import java.io.OutputStream;

public class StreamRedirector implements InputBufferProcessor {

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
