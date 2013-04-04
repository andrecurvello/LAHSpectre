package lah.spectre.multitask;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	 * Time between two scheduling, default to 2000 miliseconds (two seconds)
	 */
	private int schedule_period = 2000;

	public ScheduleTaskManager() {
		this(Executors.newSingleThreadExecutor());
	}

	public ScheduleTaskManager(ExecutorService task_executor) {
		super(task_executor);
		pending_tasks_queue = new ConcurrentLinkedQueue<T>();
		// periodically submit executable tasks
		TimerTask scheduling_task = new TaskScheduler();
		new Timer().scheduleAtFixedRate(scheduling_task, 0, schedule_period);
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

}
