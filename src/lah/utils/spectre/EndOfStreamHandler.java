package lah.utils.spectre;

import java.io.InputStream;

public interface EndOfStreamHandler {

	public void onEndOfStream(InputStream input_stream);

}
