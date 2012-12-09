package lah.utils.spectre.stream;

public interface InputBufferProcessor {

	void processBuffer(byte[] buffer, int count) throws Exception;

	void reset();

}
