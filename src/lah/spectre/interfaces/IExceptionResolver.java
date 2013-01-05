package lah.spectre.interfaces;

/**
 * Interface for object that can resolve exceptions
 * 
 * @author L.A.H.
 * 
 */
public interface IExceptionResolver {

	/**
	 * Resolve the exception, return {@literal true} if the exception is
	 * successfully resolved, {@literal false} otherwise
	 * 
	 * @param e
	 *            The exception to resolve
	 * @return {@literal true} if the exception is successfully resolved,
	 *         {@literal false} otherwise
	 * @throws Exception
	 */
	boolean resolveException(Exception e) throws Exception;

}
