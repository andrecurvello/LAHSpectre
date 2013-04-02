package lah.spectre.interfaces;

/**
 * General interface for result object.
 * 
 * This interface extends {@link IExceptionWrapper} to allow wrapping of the {@link Exception} raised in the computation
 * process. By doing this, the method does not have to throw {@link Exception}.
 * 
 * @author L.A.H.
 * 
 */
public interface IResult extends IExceptionWrapper {

	/**
	 * Indicate whether the result is already complete or is still in progress of construction; this is useful for
	 * time--consuming methods and allows progress report via this single class.
	 * 
	 * @return
	 */
	boolean isDone();

	/**
	 * Indicate whether the result is successfully computed. This call should only be valid when
	 * {@link IResult#isDone()} returns {@literal true} and must return {@literal false} whenever
	 * {@link IResult#hasException()} return {@literal true}. Note that, even if the method returns no exception, it
	 * does not mean that the result is successful.
	 * 
	 * @return {@literal true} if this result is successfully produced
	 */
	boolean isSuccessful();

}
