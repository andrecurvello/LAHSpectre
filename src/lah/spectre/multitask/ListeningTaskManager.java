package lah.spectre.multitask;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link TaskManager} extension that dispatches executable tasks whenever notified about state changes. Note that this
 * requires the submitted task to notify this manager appropriately when its state changes.
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class ListeningTaskManager<T extends Task> extends TaskManager<T> implements TaskStateListener<T> {

	/**
	 * List of tasks waiting to be scheduled/submitted for execution
	 */
	protected ConcurrentLinkedQueue<T> pending_tasks_queue;

	public ListeningTaskManager() {
		this(Executors.newSingleThreadExecutor());
	}

	public ListeningTaskManager(ExecutorService task_executor) {
		super(task_executor);
		pending_tasks_queue = new ConcurrentLinkedQueue<T>();
	}

	@Override
	public void cancel(T task) {
		super.cancel(task);
		synchronized (pending_tasks_queue) {
			// remove the task if it is pending for execution
			pending_tasks_queue.remove(task);
		}
	}

	/**
	 * Submit pending tasks which has become executable for execution
	 */
	protected void dispatchExecutableTasks() {
		synchronized (pending_tasks_queue) {
			Iterator<T> pending_task_iterator = pending_tasks_queue.iterator();
			while (pending_task_iterator.hasNext()) {
				T pending_task = pending_task_iterator.next();
				if (pending_task.isExecutable()) {
					pending_task_iterator.remove();
					submit(pending_task);
				}
			}
		}
	}

	/**
	 * Enqueue a task for scheduling
	 * 
	 * @param task
	 *            Task to enqueue, periodically check and submit once it becomes executable
	 */
	public void enqueue(T task) {
		synchronized (pending_tasks_queue) {
			if (!pending_tasks_queue.contains(task))
				pending_tasks_queue.add(task);
		}
	}

	/**
	 * Get the number of pending tasks
	 * 
	 * @return Number of pending tasks
	 */
	public int getPendingTasksCount() {
		synchronized (pending_tasks_queue) {
			return pending_tasks_queue.size();
		}
	}

	@Override
	public void onStateChanged(T task) {
		dispatchExecutableTasks();
	}

}
