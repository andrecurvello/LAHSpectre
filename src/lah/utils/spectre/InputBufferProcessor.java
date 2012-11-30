package lah.utils.spectre;

public interface InputBufferProcessor {

	void processBuffer(byte[] buffer, int count) throws Exception;

	void reset();

}
