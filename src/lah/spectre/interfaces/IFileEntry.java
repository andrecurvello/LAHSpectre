package lah.spectre.interfaces;

/**
 * Interface for an archive entry which represents a file or directory when it
 * is unarchived.
 * 
 * @author L.A.H.
 */
public interface IFileEntry {

	public String getPath();

	public boolean isDirectory();

}