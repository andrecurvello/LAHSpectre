package lah.utils.spectre;

public class StreamDisposer implements InputBufferProcessor {

	@Override
	public void processBuffer(byte[] buffer, int count) throws Exception {
		// Do nothing, just ignore the output
	}

	@Override
	public void reset() {
		// Do nothing
	}

}
