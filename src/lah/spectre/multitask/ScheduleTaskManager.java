package lah.spectre.multitask;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extension of {@link TaskManager} which periodically check if the task is executable and submit them. Unlike
 * {@link ListeningTaskManager}, this manager requires no notification effort from the task object's side and thus is
 * safer to use (for there is no fear of non-dispatching of executable tasks).
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class ScheduleTaskManager<T extends Task> extends ListeningTaskManager<T> {

	/**
	 * {@link TimerTask} to periodically check tasks' status and schedule (i.e. submit) pending executable tasks
	 * 
	 * @author L.A.H.
	 * 
	 */
	private class TaskScheduler extends TimerTask {

		@Override
		public void run() {
			dispatchExecutableTasks();
		}

	}

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
	public void onStateChanged(T task) {
		// Overriding the superclass method to disable dispatching after state update
	}

}
