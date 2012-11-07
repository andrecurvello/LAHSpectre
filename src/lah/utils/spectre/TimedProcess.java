package lah.utils.spectre;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class represents a timed {@link Process}. It has analogous functionality
 * with extra functionality to time the process and kill it if it exceeds the
 * timeout.
 * 
 * @author Vu An Hoa
 * 
 */
public class TimedProcess {

	/**
	 * Execute a process with a time out.
	 * 
	 * Note: currently, environment is not taken into account.
	 * 
	 * @param command
	 *            Tokenized program and arguments to construct process as in
	 *            {@link Runtime#exec(String[], String[], File)}
	 * @param environment
	 *            Program environment as in
	 *            {@link Runtime#exec(String[], String[], File)}
	 * @param directory
	 *            Directory to run the process as in
	 *            {@link Runtime#exec(String[], String[], File)}
	 * @param timer
	 *            A timer object to schedule the killing of the process on
	 *            timeout, input {@literal null} will generate a new object
	 * @param timeout
	 *            The time that we allow the process to run
	 * @return An instance of {@link TimedProcess} with which the caller can
	 *         decide to wait, interact via stdin/System.out.println of that
	 *         process in any way it wants.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static TimedProcess create(String[] command, String[] environment,
			File directory, File stdoutput, Timer timer, final long timeout)
			throws IOException, InterruptedException {
		// Process process = Runtime.getRuntime().exec(command, environment,
		// directory);
		ProcessBuilder proc_builder = new ProcessBuilder(command);
		proc_builder.directory(directory);
		final Process process = proc_builder.start();
		
		if (stdoutput != null)
			proc_builder.redirectOutput(stdoutput);

		if (timer == null)
			timer = new Timer();

		TimerTask kill_process_task = new TimerTask() {

			@Override
			public void run() {
				if (BuildConfig.DEBUG)
					System.out.println("TeX.execTimeOut : Process " + process
							+ " exceeds time allowance of " + timeout
							+ " miliseconds. Kill!");
				process.destroy();
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					if (BuildConfig.DEBUG)
						System.out
								.println("TeX.execTimeOut : Interrupted while waiting for process "
										+ process + " to be fully destroyed.");
				}
			}
		};
		timer.schedule(kill_process_task, timeout);
		return new TimedProcess(process, kill_process_task);
	}

	/**
	 * Similar to
	 * {@link TimedProcess#create(String[], String[], File, Timer, long)} but we
	 * are not interested in additional I/O but only the final output value of
	 * the process.
	 * 
	 * @param command
	 * @param environment
	 * @param directory
	 * @param timer
	 * @param timeout
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int execute(String[] command, String[] environment,
			File directory, File stdout, Timer timer, final long timeout)
			throws IOException, InterruptedException {
		TimedProcess timed_process = create(command, environment, directory,
				stdout, timer, timeout);
		Process process = timed_process.getProcess();
		TimerTask kill_process_task = timed_process.getProcessKillingTask();
		try {
			if (BuildConfig.DEBUG)
				System.out.println("TeX.execTimeOut : Waiting for process "
						+ process + " to finish ...");
			int result = process.waitFor();
			if (BuildConfig.DEBUG)
				System.out.println("TeX.execTimeOut : Process " + process
						+ " exits! Kill the timer and destroy the process.");
			kill_process_task.cancel();
			process.destroy();
			return result;
		} catch (InterruptedException e) {
			if (BuildConfig.DEBUG)
				System.out
						.println("TeX.execTimeOut : Interrupted while waiting for process "
								+ process + " to finish.");
			process.destroy();
			process.waitFor();
			throw e;
		}
	}

	/**
	 * An executing task that will kill the process when time out is reached
	 */
	private TimerTask kill_process_task;

	/**
	 * The external process to run
	 */
	private Process process;

	/**
	 * Private constructor to disallow direct instance construction. Instead,
	 * users are to use {@link TimedProcess#create} to create instances or a
	 * more convenient {@link TimedProcess#execute} to execute a process and
	 * obtain the exit value.
	 * 
	 * @param proc
	 * @param killer
	 */
	private TimedProcess(Process proc, TimerTask killer) {
		process = proc;
		kill_process_task = killer;
	}

	public Process getProcess() {
		return process;
	}

	public TimerTask getProcessKillingTask() {
		return kill_process_task;
	}

} // end of TimedProcess