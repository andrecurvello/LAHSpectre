package lah.utils.spectre;

import java.io.InputStream;

public class InputStreamProcessingThread extends Thread {

	private byte[] buffer;

	private ExceptionHandler exception_handler;

	private InputStream input_stream;

	private ResultListener<Void> result_listener;

	private InputBufferProcessor stream_processor;

	public InputStreamProcessingThread(InputStream inp_stream,
			InputBufferProcessor processor, ExceptionHandler exc_handler,
			ResultListener<Void> res_handler) {
		input_stream = inp_stream;
		stream_processor = processor;
		exception_handler = exc_handler;
		result_listener = res_handler;
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
			while ((count = input_stream.read(buffer)) != -1) {
				if (stream_processor != null)
					stream_processor.processBuffer(buffer, count);
			}
			if (result_listener != null)
				result_listener.onResultObtained(null);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			if (exception_handler != null)
				exception_handler.onException(e);
		}
	}
}
