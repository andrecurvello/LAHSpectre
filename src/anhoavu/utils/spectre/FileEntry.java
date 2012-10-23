package anhoavu.utils.spectre;

/**
 * Interface for an archive entry which represents a file or directory when it
 * is unarchived.
 */
public interface FileEntry {

	public String getPath();

	public boolean isDirectory();

}