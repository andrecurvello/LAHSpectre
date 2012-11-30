package lah.utils.spectre;

import java.io.File;

public interface FileDownloader {

	File downloadFile(String uri, String file_name) throws Exception;

}
