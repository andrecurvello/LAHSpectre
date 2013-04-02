package lah.spectre.multitask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Generic task manager to ease management of tasks such as adding, executing, canceling, etc
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class TaskManager<T extends Runnable> {

	/**
	 * List of task added
	 */
	private List<T> added_tasks_list;

	/**
	 * An {@link ExecutorService} to schedule execution of tasks in background
	 */
	private ExecutorService task_executor;

	/**
	 * Table mapping each task to its {@link Future} for cancellation
	 */
	private Map<T, Future<?>> task_future_table;

	public TaskManager() {
		// prepare internal data structures
		added_tasks_list = new LinkedList<T>();
		task_future_table = new HashMap<T, Future<?>>();
		task_executor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Add a new task to the list of managed task
	 * 
	 * @param task
	 *            Task to add for management
	 */
	public void add(T task) {
		synchronized (added_tasks_list) {
			if (task != null && added_tasks_list.contains(task))
				added_tasks_list.add(task);
		}
	}

	/**
	 * Cancel a task; note that this DOES NOT remove the task under supervision of this task manager
	 * 
	 * @param task
	 *            Task to cancel
	 */
	public void cancel(T task) {
		if (task == null)
			return;
		// cancel the task if it is executing
		synchronized (task_future_table) {
			Future<?> task_future = task_future_table.remove(task);
			if (task_future != null)
				task_future.cancel(true);
		}
	}

	/**
	 * Get the list of tasks under management of this instance
	 * 
	 * @return The list of task added (without removed tasks)
	 */
	public List<T> getTaskList() {
		return added_tasks_list;
	}

	/**
	 * Remove a task if it is managed by this task manager
	 * 
	 * @param task
	 *            Task to be removed
	 */
	public void remove(T task) {
		synchronized (added_tasks_list) {
			added_tasks_list.remove(task);
		}
		cancel(task);
	}

	/**
	 * Submit a (SHOULD BE pending) task for execution
	 * 
	 * @param task
	 *            Task to execute or re-execute
	 */
	public void submit(T task) {
		synchronized (task_future_table) {
			task_future_table.put(task, task_executor.submit(task));
		}
	}

}
