package lah.utils.spectre.stream;

/**
 * Interface for byte[] buffer processing classes; concrete implementations are
 * {@link StreamRedirector}.
 * 
 * @author L.A.H.
 * 
 */
public interface IBufferProcessor {

	void processBuffer(byte[] buffer, int count) throws Exception;

	void reset();

}
