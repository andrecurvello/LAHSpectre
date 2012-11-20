package lah.utils.spectre;

import java.io.IOException;
import java.io.InputStream;

public interface StreamExceptionHandler {

	void onStreamIOException(InputStream input_stream, IOException e);

}
