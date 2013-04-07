package lah.spectre.multitask;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
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
		assert task_executor != null;
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
	 * @param finished_tasks
	 *            An allocated set to collect all the newly finished tasks. Note that caller is expected to make sure
	 *            that there will be no concurrent modification; otherwise, {@link ConcurrentModificationException}
	 *            might be fired! If this parameter is {@code null} then it will be ignored.
	 * @return The number of unfinished tasks that has been submitted to this task manager
	 */
	public int getUnfinishedTaskCount(Set<T> finished_tasks) {
		synchronized (task_future_table) {
			// clean up the table
			Iterator<T> submitted_tasks_iterator = task_future_table.keySet().iterator();
			T task;
			while (submitted_tasks_iterator.hasNext()) {
				task = submitted_tasks_iterator.next();
				Future<?> task_future = task_future_table.get(task);
				if (task_future == null)
					continue;
				if (task_future.isDone()) {
					submitted_tasks_iterator.remove();
					if (finished_tasks != null)
						finished_tasks.add(task);
				}
			}
			return task_future_table.size();
		}
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
