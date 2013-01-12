package lah.spectre.stream;

import lah.spectre.process.TimedShell;

/**
 * Similar to {@link IBufferProcessor}, this object produces byte[] buffers
 * instead of consuming them. This is used in {@link TimedShell} to supply
 * standard input to external process allowing two-way interaction.
 * 
 * @author L.A.H.
 * 
 */
public interface IBufferProducer {

	/**
	 * Fill a byte array buffer
	 * 
	 * @return The number of bytes filled in the buffer, should be from
	 *         {@literal 0} to {@code buffer.length}; or {@literal -1} if there
	 *         is nothing more to fill in.
	 */
	int fillBuffer(byte[] buffer) throws Exception;

}
