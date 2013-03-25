package lah.spectre.interfaces;

/**
 * Interface for class that encapsulate an {@link Exception} internally such as
 * a result of a computation that potentially raise exception
 * 
 * @author L.A.H.
 * 
 */
public interface IExceptionWrapper {

	Exception getException();

	boolean hasException();

	// void setException(Exception e);

}
