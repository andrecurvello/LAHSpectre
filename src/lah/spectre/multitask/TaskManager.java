package lah.spectre.multitask;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Generic task manager to ease management of tasks such as adding, scheduling,
 * canceling, etc
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class TaskManager<T extends Task> {

	/**
	 * List of tasks waiting to be scheduled for execution
	 */
	private List<T> pending_task_queue;

	/**
	 * An {@link ExecutorService} to schedule execution of tasks in background
	 */
	private ExecutorService task_executor;

	/**
	 * We use the address of the task to identify it
	 */
	private Map<Integer, T> task_id_table;

	/**
	 * Table mapping task ID to {@link Future} objects for cancellation
	 */
	private Map<Integer, Future<?>> future_id_table;

	public TaskManager() {
		task_id_table = new TreeMap<Integer, T>();
		future_id_table = new TreeMap<Integer, Future<?>>();
		task_executor = Executors.newSingleThreadExecutor();
		pending_task_queue = new LinkedList<T>();
		// TODO start the scheduler thread which periodically
		// submit executable tasks to the executor
	}

	public void addTask(final T task) {
		if (task != null) {
			task_id_table.put(System.identityHashCode(task), task);
		}
	}

	public void cancel(T task) {
		if (task != null) {
			// remove the task if it is pending
			pending_task_queue.remove(task);

			// cancel the task if it is executing
			Future<?> task_future = future_id_table.get(System
					.identityHashCode(task));
			if (task_future != null)
				task_future.cancel(true);
		}
	}

	public void enqueue(T task) {
		pending_task_queue.add(task);
		// TODO move the remaining code to the scheduler thread
		// if the task is executable, submit it
		Future<?> future = task_executor.submit(task);
		future_id_table.put(System.identityHashCode(task), future);
	}

	public void removeTask(int id) {
		T task = task_id_table.remove(id);
		if (task != null)
			cancel(task);
	}

}
