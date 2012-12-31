package lah.utils.spectre.stream;

/**
 * Implementation of {@link InputBufferProcessor} that accumulate the buffer
 * into {@link StringBuilder}
 * 
 * @author L.A.H.
 * 
 */
public class StringOfStreamAccumulator implements InputBufferProcessor {

	StringBuilder result;

	public String getResult() {
		return (result == null ? null : result.toString());
	}

	@Override
	public void processBuffer(byte[] buffer, int count) {
		result.append(new String(buffer, 0, count));
	}

	@Override
	public void reset() {
		result = new StringBuilder();
	}

}
