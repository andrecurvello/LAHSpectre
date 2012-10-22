package anhoavu.utils.spectre;

/**
 * Collection of methods to process file name and extension
 * 
 * @author Vu An Hoa
 * 
 */
public class FileName {

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
