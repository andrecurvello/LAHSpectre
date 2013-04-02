package lah.spectre.multitask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
	 * An {@link ExecutorService} to schedule execution of tasks in background
	 */
	private ExecutorService task_executor;

	/**
	 * Table mapping each task to its {@link Future} for cancellation
	 */
	private Map<T, Future<?>> task_future_table;

	/**
	 * Construct a manager with a default single thread executor obtained from
	 * {@link Executors#newSingleThreadExecutor()}
	 */
	public TaskManager() {
		this(Executors.newSingleThreadExecutor());
	}

	/**
	 * Construct a manager with a specified {@link ExecutorService} to execute tasks
	 * 
	 * @param task_executor
	 */
	public TaskManager(ExecutorService task_executor) {
		this.task_executor = task_executor;
		this.task_future_table = new HashMap<T, Future<?>>();
	}

	/**
	 * Cancel a task if it is executing
	 * 
	 * NOTE: this only works if the task is previously submitted via {@link TaskManager#submit} method of this object
	 * 
	 * @param task
	 *            Task to cancel
	 */
	public void cancel(T task) {
		synchronized (task_future_table) {
			Future<?> task_future = task_future_table.remove(task);
			if (task_future != null)
				task_future.cancel(true);
		}
	}

	/**
	 * Get the number of unfinished tasks that were previously submitted
	 * 
	 * @return
	 */
	public int getUnfinishedTaskCount() {
		synchronized (task_future_table) {
			// clean up the table
			Set<T> submitted_tasks = task_future_table.keySet();
			for (T task : submitted_tasks) {
				Future<?> task_future = task_future_table.get(task);
				if (task_future == null)
					continue;
				if (task_future.isDone() || task_future.isCancelled())
					task_future_table.remove(task);
			}
		}
		return task_future_table.size();
	}

	/**
	 * Submit a (SHOULD be pending) task for execution
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
