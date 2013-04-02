package lah.spectre.multitask;

import java.util.concurrent.ExecutorService;

/**
 * General interface for a task extending {@link Runnable} with a method to indicate whether an instance is ready for
 * execution; if it is, it can be submitted to the {@link ExecutorService} for execution. Object implement this
 * interface SHOULD NOT override the equal() method because task manager uses {@link System#identityHashCode(Object)} to
 * determine equality.
 * 
 * @author L.A.H.
 * 
 */
public interface Task extends Runnable {

	/**
	 * Check if this task is executable. This method should be implemented based the context (i.e. meaning of the task).
	 * 
	 * For example, task can have other tasks as dependency and is only executable if their dependencies are met.
	 * 
	 * In another context, tasks might require resources to be available such as displaying the image requires the image
	 * file to be retrieved over the network.
	 * 
	 * WARNING NOTE: This method is EXTREMELY state-sensitive. Therefore, implementations SHOULD synchronize all
	 * state-changing methods which affect this method.
	 * 
	 * @return {@code true} if this task is executable; {@code false} otherwise.
	 */
	boolean isExecutable();

}
