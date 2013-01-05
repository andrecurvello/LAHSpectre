package lah.spectre.stream;

import java.io.InputStream;

import lah.spectre.BuildConfig;
import lah.spectre.interfaces.IExceptionHandler;
import lah.spectre.interfaces.IResultListener;

public class BufferProcessingThread extends Thread {

	private byte[] buffer;

	private IExceptionHandler exception_handler;

	private InputStream input_stream;

	private IResultListener<Void> result_listener;

	private IBufferProcessor stream_processor;

	public BufferProcessingThread(InputStream inp_stream,
			IBufferProcessor processor, IExceptionHandler exc_handler,
			IResultListener<Void> res_handler) {
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
