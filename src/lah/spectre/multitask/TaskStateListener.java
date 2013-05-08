package lah.spectre.multitask;

/**
 * Interface for object which listens to task state updates
 * 
 * @author L.A.H.
 * 
 */
public interface TaskStateListener<T extends Task> {

	/**
	 * Call back method to invoke when state of task is changed
	 * 
	 * @param task
	 *            The task whose state has just changed
	 */
	void onStateChanged(T task);

}