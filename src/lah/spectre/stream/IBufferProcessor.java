package lah.spectre.stream;

/**
 * Interface for byte[] buffer processing classes; concrete implementations are
 * {@link StreamRedirector} and {@link StringAccumulator}.
 * 
 * @author L.A.H.
 * 
 */
public interface IBufferProcessor {

	/**
	 * Process the bytes in buffer[0..count-1]
	 * 
	 * @param buffer
	 * @param count
	 * @throws Exception
	 */
	void processBuffer(byte[] buffer, int count) throws Exception;

}
