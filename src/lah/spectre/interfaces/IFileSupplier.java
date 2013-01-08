package lah.spectre.interfaces;

import java.io.File;

/**
 * Interface for a file supplier
 * 
 * @author L.A.H.
 * 
 */
public interface IFileSupplier {

	/**
	 * Get the {@link File} of specified name
	 * 
	 * @param file_name
	 * @return
	 * @throws Exception
	 */
	File getFile(String file_name) throws Exception;

}
