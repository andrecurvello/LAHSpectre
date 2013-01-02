package lah.utils.spectre.interfaces;

/**
 * Interface for an archive entry which represents a file or directory when it
 * is unarchived.
 */
public interface IFileEntry {

	public String getPath();

	public boolean isDirectory();

}