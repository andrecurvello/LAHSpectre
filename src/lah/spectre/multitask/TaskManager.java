package lah.spectre.multitask;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Generic task manager to ease adding, starting, cancel, ... of tasks
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class TaskManager<T extends Task> {

	/**
	 * We use the address of the task to identify it
	 */
	private Map<Integer, T> task_id_table;

	/**
	 * An {@link ExecutorService} to schedule execution of tasks in background
	 */
	private ExecutorService task_executor;

	public TaskManager() {
		task_id_table = new TreeMap<Integer, T>();
		task_executor = Executors.newSingleThreadExecutor();
	}

	public void addTask(final T task) {
		if (task != null) {
			task_id_table.put(System.identityHashCode(task), task);
			Future<?> future = task_executor.submit(new Runnable() {

				@Override
				public void run() {
					task.start();
					System.out.println("Task " + task + " is finished.");
				}
			});
			task.setFuture(future);
		}
	}

	public void removeTask(int id) {
		task_id_table.remove(id);
	}

}
