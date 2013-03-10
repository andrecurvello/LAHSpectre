package lah.spectre.multitask;

import java.util.Map;
import java.util.TreeMap;

/**
 * Generic task manager to ease adding, starting, cancel, ... of tasks
 * 
 * @author L.A.H.
 * 
 * @param T
 *            Generic type for tasks
 */
public class TaskManager<T> {

	/**
	 * We use the address of the task to identify it
	 */
	private Map<Integer, T> task_id_table;

	public TaskManager() {
		task_id_table = new TreeMap<Integer, T>();
	}

	public void addTask(T task) {
		if (task != null)
			task_id_table.put(System.identityHashCode(task), task);
	}

	public void removeTask(int id) {
		task_id_table.remove(id);
	}

}
