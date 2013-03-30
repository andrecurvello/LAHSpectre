package lah.spectre;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Collection of methods to process file name and extension
 * 
 * @author L.A.H.
 * 
 */
public class FileName {

	/**
	 * Prefix for a temporary file name
	 */
	private static final String TEMP_FILE_NAME = "temp.spectreget";

	/**
	 * Pick a valid file name for a temporary file in a directory.
	 * 
	 * @param directory
	 *            The directory in which we want to create a new temp file, assuming to be an existing & readable
	 *            directory
	 * @return A name for a new file in output_directory, this file name is of the form
	 *         {@link SpectreGet#TEMP_FILE_NAME} followed by a suffix of form "(<integer>)" such as (0), (1), ...
	 */
	public static String createNewTemporaryFileName(String directory) {
		assert (directory != null);

		File dir = new File(directory);
		assert (dir.exists() && dir.canRead());

		// List the files starting with {@link SpectreGet#TEMP_FILE_NAME}
		String[] tmpfiles = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(TEMP_FILE_NAME);
			}
		});

		if (tmpfiles.length == 0)
			return TEMP_FILE_NAME + "(0)";

		// Select a new suffix
		Arrays.sort(tmpfiles);
		for (int i = 0;; i++) {
			String fname = TEMP_FILE_NAME + "(" + i + ")";
			if (Arrays.binarySearch(tmpfiles, fname) >= 0)
				return fname;
		}
	}

	/**
	 * Get the file extension
	 * 
	 * @param name
	 * @return
	 */
	public static String getExtension(String name) {
		int dot = name.lastIndexOf('.');
		if (dot == -1 || dot == name.length() - 1)
			return "";
		else
			return name.substring(dot + 1);
	}

	/**
	 * Find the file name from the path
	 * 
	 * @param file_path
	 * @param path_sep
	 * @return
	 */
	public static String getName(String file_path, char path_sep) {
		int last_path_sep = file_path.lastIndexOf(path_sep);
		if (last_path_sep != -1)
			return file_path.substring(last_path_sep + 1);
		// this is the case when file_path actually consist of only the file
		// name
		return file_path;
	}

	/**
	 * Trim file extension, copied from {@link http ://stackoverflow.com/questions
	 * /941272/how-do-i-trim-a-file-extension-from-a-string-in-java}
	 * 
	 * @param file_name
	 * @return
	 */
	public static String removeFileExtension(String file_name) {
		String separator = System.getProperty("file.separator");
		String file_name_no_ext;

		// Remove the path upto the filename.
		int lastSeparatorIndex = file_name.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			file_name_no_ext = file_name;
		} else {
			file_name_no_ext = file_name.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = file_name_no_ext.lastIndexOf(".");
		if (extensionIndex == -1)
			return file_name_no_ext;

		return file_name_no_ext.substring(0, extensionIndex);
	}

	/**
	 * @param file_name
	 * @param new_ext
	 * @return
	 */
	public static String replaceFileExt(String file_name, String new_ext) {
		return removeFileExtension(file_name) + "." + new_ext;
	}

}
