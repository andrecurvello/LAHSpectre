package lah.spectre.multitask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Generic task manager to ease management of tasks such as adding, scheduling, canceling, etc
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class TaskManager<T extends Task> {

	/**
	 * {@link TimerTask} to periodically check tasks' status and schedule (i.e. submit) pending executable tasks
	 * 
	 * @author L.A.H.
	 * 
	 */
	private class TaskScheduler extends TimerTask {

		@Override
		public void run() {
			synchronized (pending_tasks_queue) {
				Iterator<T> pending_task_iterator = pending_tasks_queue.iterator();
				while (pending_task_iterator.hasNext()) {
					T pending_task = pending_task_iterator.next();
					if (pending_task.isExecutable()) {
						pending_task_iterator.remove();
						task_future_table.put(pending_task, task_executor.submit(pending_task));
					}
				}
			}
		}

	}

	/**
	 * Time between two scheduling, set to 100 miliseconds
	 */
	private static final int SCHEDULE_PERIOD = 200;

	/**
	 * List of task added
	 */
	private List<T> added_task_list;

	/**
	 * List of tasks waiting to be scheduled/submitted for execution
	 */
	private ConcurrentLinkedQueue<T> pending_tasks_queue;

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
		task_future_table = new HashMap<T, Future<?>>();
		task_executor = Executors.newSingleThreadExecutor();
		added_task_list = new ArrayList<T>();
		pending_tasks_queue = new ConcurrentLinkedQueue<T>();

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
		// add task if it is not already in the manage list
		synchronized (added_task_list) {
			if (!added_task_list.contains(task))
				added_task_list.add(task);
		}
		// enqueue task for scheduling
		synchronized (pending_tasks_queue) {
			if (!pending_tasks_queue.contains(task))
				pending_tasks_queue.add(task);
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
		synchronized (pending_tasks_queue) {
			pending_tasks_queue.remove(task);
		}
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
		synchronized (added_task_list) {
			added_task_list.remove(task);
		}
		cancel(task);
	}

}
