package lah.spectre.multitask;

/**
 * General enumeration of task states
 * 
 * @author L.A.H.
 * 
 */
public enum TaskState {
	/**
	 * Task completed without error
	 */
	COMPLETE,
	/**
	 * Error/exception is encountered during execution
	 */
	ERROR,
	/**
	 * Task is executing i.e. run() is executed
	 */
	EXECUTING,
	/**
	 * Task is waiting for execution
	 */
	PENDING;
}
