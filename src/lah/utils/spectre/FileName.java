package lah.utils.spectre;

/**
 * Collection of methods to process file name and extension
 * 
 * @author Vu An Hoa
 * 
 */
public class FileName {

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
	 * Trim file extension, copied from {@link http
	 * ://stackoverflow.com/questions
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
