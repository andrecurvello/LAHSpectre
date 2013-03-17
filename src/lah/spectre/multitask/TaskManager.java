package lah.spectre.multitask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
	 * {@link TimerTask} to periodically check tasks' status and schedule (i.e.
	 * submit) pending executable tasks
	 * 
	 * @author L.A.H.
	 * 
	 */
	private class TaskScheduler extends TimerTask {

		@Override
		public void run() {
			synchronized (pending_task_list) {
				for (T task : pending_task_list) {
					if (task.isExecutable()) {
						task_executor.submit(task);
						pending_task_list.remove(task);
						Future<?> future = task_executor.submit(task);
						future_id_table.put(System.identityHashCode(task),
								future);
					}
				}
			}
		}

	}

	/**
	 * Time between two scheduling, set to 100 miliseconds
	 */
	private static final int SCHEDULE_PERIOD = 100;

	/**
	 * List of task added
	 */
	private List<T> added_task_list;

	/**
	 * Table mapping task ID to {@link Future} objects for cancellation
	 */
	private Map<Integer, Future<?>> future_id_table;

	/**
	 * List of tasks waiting to be scheduled/submitted for execution
	 */
	private List<T> pending_task_list;

	/**
	 * An {@link ExecutorService} to schedule execution of tasks in background
	 */
	private ExecutorService task_executor;

	/**
	 * We use the address of the task to identify it
	 */
	private Map<Integer, T> task_id_table;

	public TaskManager() {
		task_id_table = new TreeMap<Integer, T>();
		future_id_table = new TreeMap<Integer, Future<?>>();
		task_executor = Executors.newSingleThreadExecutor();
		added_task_list = new ArrayList<T>();
		pending_task_list = new ArrayList<T>();
		// periodically submit executable tasks
		TimerTask scheduling_task = new TaskScheduler();
		new Timer().scheduleAtFixedRate(scheduling_task, 0, SCHEDULE_PERIOD);
	}

	/**
	 * Add a new task, waiting to be scheduled
	 * 
	 * @param task
	 */
	public void add(T task) {
		if (task == null)
			return;
		if (!added_task_list.contains(task)) {
			task_id_table.put(System.identityHashCode(task), task);
			added_task_list.add(task);
		}
		// submit the task if it is executable; otherwise, put it to the
		// list of tasks to be periodically checked and submitted by the
		// scheduling thread
		if (task.isExecutable()) {
			task_executor.submit(task);
		} else {
			synchronized (pending_task_list) {
				pending_task_list.add(task);
			}
		}
	}

	/**
	 * Cancel a task
	 * 
	 * @param task
	 */
	public void cancel(T task) {
		if (task == null)
			return;
		// remove the task if it is pending for execution
		synchronized (pending_task_list) {
			pending_task_list.remove(task);
		}
		// and cancel the task if it is executing
		Future<?> task_future = future_id_table.remove(System
				.identityHashCode(task));
		if (task_future != null)
			task_future.cancel(true);
	}

	/**
	 * Get the list of tasks under management of this instance
	 * 
	 * @return the list of task added (without removed tasks)
	 */
	public List<T> getTaskList() {
		return added_task_list;
	}

	/**
	 * Remove a task from management by this instance
	 * 
	 * @param id
	 */
	public void remove(T task) {
		int id = System.identityHashCode(task);
		task_id_table.remove(id);
		added_task_list.remove(task);
		if (task != null)
			cancel(task);
	}

}
