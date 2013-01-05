package lah.spectre.interfaces;

import java.io.File;

/**
 * Interface for a file supplier
 * 
 * @author L.A.H.
 *
 */
public interface IFileSupplier {

	File getFile(String file_name) throws Exception;

}
