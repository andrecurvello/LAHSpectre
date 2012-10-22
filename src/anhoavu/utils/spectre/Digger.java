package anhoavu.utils.spectre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.tukaani.xz.XZInputStream;

/**
 * This class serves as a universal {@link InputStream} for streams of
 * compressed (a file containing compressed data) or archived (a file
 * encapsulating a whole directory structure) input.
 */
public class Digger extends InputStream {

	public static enum CompressFormat {
		GZIP, XZ
	}

	/**
	 * Interface for an archive entry which represents a file or directory when
	 * it is unarchived.
	 */
	public interface Entry {

		public String getName();

		public boolean isDirectory();

	}

	/**
	 * Interface for objects which relocates files within an archive
	 */
	public interface FileRelocator {

		public String relocate(String file_path);

	}

	/**
	 * Recognized format
	 */
	public enum Format {
		BZIP2, GZIP, TAR, XZ, ZIP
	}

	/**
	 * Simple {@link FileRelocator} that relocate files relative to a directory
	 */
	public class SingleRootFileRelocator implements FileRelocator {

		private String root_directory;

		public SingleRootFileRelocator(String root_directory) {
			this.root_directory = root_directory;
		}

		public String relocate(String file_path) {
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
	public static InputStream decompress(CompressFormat format,
			File compressed_file) throws IOException {
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
	public static InputStream decompress(CompressFormat format,
			InputStream compressed_src) throws IOException {
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
	public static InputStream decompress(CompressFormat format,
			String compressed_file_path) throws IOException {
		return decompress(format, new File(compressed_file_path));
	}

	/**
	 * Extract the package to the TeXMF tree
	 * 
	 * @return {@literal true} if the file is extracted fully; {@literal false}
	 *         if any error occurs.
	 */
	public static boolean extractPackageFile(File pkg_file,
			String output_directory) {
		InputStream p_tar_xz_inpstr = null; // InputStream connecting to the
											// downloaded file
		XZInputStream p_tar_inpstr = null; // InputStream to the xz uncompressed
											// file
		TarInputStream p_inpstr = null; // InputStream to the xz uncompressed &
										// untar archive

		try {
			p_tar_xz_inpstr = new FileInputStream(pkg_file);
			p_tar_inpstr = new XZInputStream(p_tar_xz_inpstr);

			p_inpstr = new TarInputStream(p_tar_inpstr);

			assert (p_tar_xz_inpstr != null);
			assert (p_tar_inpstr != null);
			assert (p_inpstr != null);

			// Write files and directory in the tar archive
			TarEntry entry;
			while ((entry = p_inpstr.getNextEntry()) != null) {
				// Break the loop when we are interrupted
				// TODO make it safer, deleting existing files for example!
				if (Thread.currentThread().isInterrupted())
					return false;

				// Absolute path to the [entry] after installation
				// If the top directory is a "texmf*/" (i.e. either texmf
				// or texmf-dist) or a "bin/" then it does not have to be
				// relocated; otherwise, it has to be relocated under
				// $TEXMFROOT/texmf-dist/
				String abs_entry_path = null;
				if (entry.getName().startsWith("texmf")
						|| entry.getName().startsWith("bin")
						|| entry.getName().startsWith("tlpkg"))
					abs_entry_path = output_directory + "/" + entry.getName(); // getPathToTeXMFRootDirectory()
				else
					abs_entry_path = output_directory //
							+ "/texmf-dist/" + entry.getName();
				if (entry.isDirectory()) {
					File entry_dir = new File(abs_entry_path);
					// System.out.println("Add directory " + entry_dir);
					entry_dir.mkdirs();
				} else {
					File entry_file = new File(abs_entry_path);
					// System.out.println("Add file " + entry_file);
					entry_file.getParentFile().mkdirs(); // make necessary
															// directories
					FileOutputStream fos = new FileOutputStream(entry_file);
					Streams.pipeIOStream(p_inpstr, fos); // pipe
					// tar_is
					// directly
					// to
					// file!
					fos.close();
				}
			}
			return true;
		} catch (FileNotFoundException e) {
			if (BuildConfig.DEBUG)
				System.out.println("TeX.extractPackageFile : Input file "
						+ pkg_file + " does not exist!");
			e.printStackTrace();
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				System.out.println("TeX.extractPackageFile : I/O error "
						+ pkg_file + " during extraction!");
			e.printStackTrace();
		} finally {
			try {
				if (p_tar_xz_inpstr != null)
					p_tar_xz_inpstr.close();
				if (p_tar_inpstr != null)
					p_tar_inpstr.close();
				if (p_inpstr != null)
					p_inpstr.close();
			} catch (IOException e) {
				if (BuildConfig.DEBUG)
					System.out
							.println("TeX.extractPackageFile : IO error - cannot close streams.");
				e.printStackTrace();
			}
		}
		return false;
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

	public Entry getNextEntry() {
		return null;
	}

	@Override
	public int read() throws IOException {
		return input_stream.read();
	}

	/**
	 * Extract an archive stream to the file system
	 * 
	 * @param archive_stream
	 * @param relocator
	 * @throws IOException
	 */
	public void unarchiveToFileSystem(FileRelocator relocator)
			throws IOException {
		Entry entry;
		while ((entry = getNextEntry()) != null) {

			if (Thread.currentThread().isInterrupted())
				break;

			// Locate the entry in the file system
			File entry_out = new File(relocator.relocate(entry.getName()));

			if (entry.isDirectory()) {
				// Make the directory
				entry_out.mkdirs();
			} else {
				// Make necessary directories first
				entry_out.getParentFile().mkdirs();
				// And then write the file
				FileOutputStream entry_outstream = new FileOutputStream(
						entry_out);
				Streams.pipeIOStream(this, entry_outstream);
				// Finally close the stream
				entry_outstream.close();
			}
		}
		close();
	}

	@Override
	public void close() throws IOException {
		input_stream.close();
	}

	@Override
	public int available() throws IOException {
		return input_stream.available();
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

}
