package lah.spectre.interfaces;

import java.io.File;

/**
 * Interface for a file downloader
 * 
 * @author L.A.H.
 * 
 */
public interface IFileDownloader {

	File downloadFile(String uri, String file_name) throws Exception;

}
