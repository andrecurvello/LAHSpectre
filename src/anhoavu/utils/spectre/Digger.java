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

public class Digger {

	public static final int TAR = 0;

	public static final int XZ = 0;

	public static InputStream decompress(int format, File compressed_file)
			throws IOException {
		return decompress(format, new FileInputStream(compressed_file));
	}

	public static InputStream decompress(int format, InputStream compressed_src)
			throws IOException {
		return new XZInputStream(compressed_src);
	}

	public static InputStream decompress(int format, String compressed_file_path)
			throws IOException {
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

}
