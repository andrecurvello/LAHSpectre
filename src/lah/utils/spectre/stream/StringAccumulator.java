package lah.utils.spectre.stream;

/**
 * Implementation of {@link IBufferProcessor} that accumulate the buffers
 * into a single {@link StringBuilder}. This is handy to collect standard output
 * of an external process.
 * 
 * @author L.A.H.
 * 
 */
public class StringAccumulator implements IBufferProcessor {

	private StringBuilder result;

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
