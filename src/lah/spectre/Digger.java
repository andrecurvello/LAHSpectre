package lah.spectre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lah.spectre.interfaces.IFileEntry;
import lah.spectre.stream.Streams;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.tukaani.xz.XZInputStream;

/**
 * This class serves as a universal {@link InputStream} for streams of
 * compressed (a file containing compressed data) or archived (a file
 * encapsulating a whole directory structure) input.
 */
public class Digger extends InputStream {

	/**
	 * Interface for objects which relocates files within an archive
	 */
	public interface FileRelocator {

		public String locate(String path);

	}

	/**
	 * Recognized format
	 */
	public enum Format {
		BZIP2, GZIP, TAR, XZ, ZIP
	}

	public interface Listener {

		void notifyCurrentProgress(int progress);

		void notifyMaxProgress(int max_progress);

	}

	/**
	 * Simple {@link FileRelocator} that relocate files relative to a directory
	 */
	public class SingleRootFileRelocator implements FileRelocator {

		private String root_directory;

		public SingleRootFileRelocator(String root_directory) {
			this.root_directory = root_directory;
		}

		public String locate(String file_path) {
			return root_directory + file_path;
		}

	}

	/**
	 * Get an {@link InputStream} to the decompressed content of a compressed
	 * file represented by a {@link File} object.
	 * 
	 * @param format
	 * @param compressed_file
	 * @return
	 * @throws IOException
	 */
	public static InputStream decompress(Format format, File compressed_file) throws IOException {
		return decompress(format, new FileInputStream(compressed_file));
	}

	/**
	 * Get an {@link InputStream} to the decompressed content of a compressed
	 * stream
	 * 
	 * @param format
	 * @param compressed_src
	 * @return
	 * @throws IOException
	 */
	public static InputStream decompress(Format format, InputStream compressed_src) throws IOException {
		return new XZInputStream(compressed_src);
	}

	/**
	 * Get an {@link InputStream} to the decompressed content of a compressed
	 * file given the {@link String} path to it.
	 * 
	 * @param format
	 * @param compressed_file_path
	 * @return
	 * @throws IOException
	 */
	public static InputStream decompress(Format format, String compressed_file_path) throws IOException {
		return decompress(format, new File(compressed_file_path));
	}

	/**
	 * Unarchive a tar stream into the file system, relocating them using the
	 * locator
	 * 
	 * @param tar_stream
	 * @param relocator
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void untarToFileSystem(TarInputStream tar_stream, FileRelocator relocator, Listener listener)
			throws IOException, InterruptedException {
		TarEntry entry;
		if (listener != null)
			listener.notifyMaxProgress(-1);
		int num_entries_processed = 0;
		while ((entry = tar_stream.getNextEntry()) != null) {

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Interrupted while extracting file.");
			}

			// Locate the entry in the file system
			File entry_out = new File(relocator.locate(entry.getName()));

			if (entry.isDirectory()) {
				// Make the directory
				entry_out.mkdirs();
			} else {
				// Make necessary directories first
				entry_out.getParentFile().mkdirs();
				// And then write the file, delete when interrupted
				try {
					Streams.streamToFile(tar_stream, entry_out, true, false);
					num_entries_processed++;
					if (listener != null)
						listener.notifyCurrentProgress(num_entries_processed);
				} catch (InterruptedException e) {
					throw new InterruptedException("Interrupted while extracting file.");
				} catch (IOException e) {
					throw new IOException("Cannot write file.");
				}
			}
		}
		tar_stream.close();
	}

	/**
	 * The compressed/archive format of this {@link Digger} object
	 */
	private Format format;

	/**
	 * The input stream that this archive binds to
	 */
	private InputStream input_stream;

	public Digger(Format fmt, InputStream stream) {
		format = fmt;
		input_stream = stream;

		// Make sure that the format matches the input streams
		switch (format) {
		case XZ:
			assert (input_stream instanceof XZInputStream);
		case TAR:
			assert (input_stream instanceof TarInputStream);
		default:
			break;
		}
	}

	@Override
	public int available() throws IOException {
		return input_stream.available();
	}

	@Override
	public void close() throws IOException {
		input_stream.close();
	}

	public IFileEntry getNextEntry() {
		return null;
	}

	@Override
	public void mark(int readlimit) {
		input_stream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return input_stream.markSupported();
	}

	@Override
	public int read() throws IOException {
		return input_stream.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return input_stream.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return input_stream.read(b, off, len);
	}

	@Override
	public void reset() throws IOException {
		input_stream.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return input_stream.skip(n);
	}

	/**
	 * Extract an archive stream to the file system
	 * 
	 * @param archive_stream
	 * @param relocator
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void unarchiveToFileSystem(FileRelocator relocator) throws IOException, InterruptedException {
		IFileEntry entry;
		while ((entry = getNextEntry()) != null) {

			if (Thread.currentThread().isInterrupted())
				break;

			// Locate the entry in the file system
			File entry_out = new File(relocator.locate(entry.getPath()));

			if (entry.isDirectory()) {
				// Make the directory
				entry_out.mkdirs();
			} else {
				// Make necessary directories first
				entry_out.getParentFile().mkdirs();
				// And then write the file
				FileOutputStream entry_outstream = new FileOutputStream(entry_out);
				Streams.pipeIOStream(this, entry_outstream);
				// Finally close the stream
				entry_outstream.close();
			}
		}
		close();
	}

}
