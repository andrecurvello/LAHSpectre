package lah.utils.spectre.stream;

import java.io.InputStream;

import lah.utils.spectre.BuildConfig;
import lah.utils.spectre.interfaces.ExceptionHandler;
import lah.utils.spectre.interfaces.ResultListener;

public class BufferProcessingThread extends Thread {

	private byte[] buffer;

	private ExceptionHandler exception_handler;

	private InputStream input_stream;

	private ResultListener<Void> result_listener;

	private IBufferProcessor stream_processor;

	public BufferProcessingThread(InputStream inp_stream,
			IBufferProcessor processor, ExceptionHandler exc_handler,
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
			// e.printStackTrace(System.out);
			if (exception_handler != null)
				exception_handler.onException(e);
		}
	}
}
