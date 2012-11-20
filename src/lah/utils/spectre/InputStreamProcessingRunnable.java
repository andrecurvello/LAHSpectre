package lah.utils.spectre;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamProcessingRunnable implements Runnable {

	private InputStream input_stream;

	private InputBufferProcessor stream_processor;

	private StreamExceptionHandler stream_exception_handler;

	private EndOfStreamHandler end_of_stream_handler;

	private byte[] buffer;

	public InputStreamProcessingRunnable(InputStream inp_stream,
			InputBufferProcessor processor, StreamExceptionHandler exc_handler,
			EndOfStreamHandler eostr_handler) {
		input_stream = inp_stream;
		stream_processor = processor;
		stream_exception_handler = exc_handler;
		end_of_stream_handler = eostr_handler;
	}

	@Override
	public void run() {
		if (input_stream == null)
			return;

		int count;
		try {
			if (buffer == null)
				buffer = new byte[BuildConfig.BUFFER_SIZE];
			if (stream_processor != null)
				stream_processor.reset();
			while ((count = input_stream.read(buffer)) != -1)
				if (stream_processor != null)
					stream_processor.processBuffer(buffer, count);
			if (end_of_stream_handler != null)
				end_of_stream_handler.onEndOfStream(input_stream);
		} catch (IOException e) {
			if (stream_exception_handler != null)
				stream_exception_handler.onStreamIOException(input_stream, e);
		}
	}

}
