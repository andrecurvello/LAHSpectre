package lah.utils.spectre.stream;

public class StringOfStreamAccumulator implements InputBufferProcessor {

	StringBuffer result;

	@Override
	public void processBuffer(byte[] buffer, int count) {
		result.append(new String(buffer, 0, count));
	}

	@Override
	public void reset() {
		result = new StringBuffer();
	}

}
