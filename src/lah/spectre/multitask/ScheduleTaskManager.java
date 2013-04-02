package lah.spectre.multitask;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Extension of {@link TaskManager} which periodically check if the task is executable and submit them
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class ScheduleTaskManager<T extends Task> extends TaskManager<T> {

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
						submit(pending_task);
					}
				}
			}
		}

	}

	/**
	 * List of tasks waiting to be scheduled/submitted for execution
	 */
	private ConcurrentLinkedQueue<T> pending_tasks_queue;

	/**
	 * Time between two scheduling, set to 500 miliseconds (half a second)
	 */
	private int schedule_period = 500;

	public ScheduleTaskManager() {
		pending_tasks_queue = new ConcurrentLinkedQueue<T>();
		// periodically submit executable tasks
		TimerTask scheduling_task = new TaskScheduler();
		new Timer().scheduleAtFixedRate(scheduling_task, 0, schedule_period);
	}

	public void add(T task, boolean schedule) {
		super.add(task);
		if (schedule) // enqueue task for scheduling if requested
			schedule(task);
	}

	@Override
	public void cancel(T task) {
		super.cancel(task);
		// remove the task if it is pending for execution
		synchronized (pending_tasks_queue) {
			pending_tasks_queue.remove(task);
		}
	}

	public void schedule(T task) {
		synchronized (pending_tasks_queue) {
			if (!pending_tasks_queue.contains(task))
				pending_tasks_queue.add(task);
		}
	}

}
