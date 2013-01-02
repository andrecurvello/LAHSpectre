package lah.utils.spectre.process;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import lah.utils.spectre.stream.IBufferProcessor;
import lah.utils.spectre.stream.Streams;

/**
 * Shell with command time-out and standard output processing
 * 
 * @author L.A.H.
 * 
 */
public class TimedShell {

	private class ProcessKillingTimerTask extends TimerTask {

		@Override
		public void run() {
			// Set time out flag
			is_timeout = true;

			// And destroy the running process; assuming that once the
			// process is destroyed, its standard output reaches
			// end-of-file.
			if (process != null)
				process.destroy();

			// Note: must not close streams here because we might still be
			// processing the standard output; simply destroy the process so
			// that the stream is EOF, the processing exits naturally.
		}

	}

	/**
	 * Flag to indicate if the process exceeds its time out limit
	 */
	private boolean is_timeout;

	/**
	 * Current executing process
	 */
	private Process process;

	/**
	 * {@link TimerTask} to kill the running process when time out is reached
	 */
	private TimerTask process_killer;

	/**
	 * Global timer to timeout the external process
	 */
	private final Timer process_timer = new Timer();

	/**
	 * Execute the command with {@literal null} stdout processor and no time out
	 * 
	 * @param command
	 *            the command to execute
	 * @param directory
	 *            the working directory to run
	 * @return exit value of the command
	 * @throws Exception
	 *             should be either {@link IOException} or
	 *             {@link TimeoutException}
	 */
	public int fork(String[] command, File directory) throws Exception {
		return fork(command, directory, null, 0);
	}

	/**
	 * Fork a new process to execute a command. This method is synchronized and
	 * waiting for standard output so it is blocking.
	 * 
	 * @param command
	 *            a command to run
	 * @param directory
	 *            the working directory to run the command
	 * @param stdout_processor
	 *            object to process the standard output, if this input is
	 *            {@literal null}, the output is simply ignored (the effect is
	 *            similar to sending to /dev/null)
	 * @param timeout
	 *            maximum allowable time for the process to execute if greater
	 *            than 0; input 0 means no timing or unlimited allowance
	 * @return the exit value of the executed command
	 * @throws Exception
	 *             {@link TimeoutException} if the timeout is reached and the
	 *             process has not finished; or any exception raised by
	 *             <b>stdout_processor</b> while processing the standard output.
	 */
	public synchronized int fork(String[] command, File directory,
			IBufferProcessor stdout_processor, long timeout)
			throws Exception {
		is_timeout = false;
		process = new ProcessBuilder(command).directory(directory)
				.redirectErrorStream(true).start();

		// Schedule the timer to time-out the process (if necessary)
		// Note that we have to recreate the TimerTask again and again since
		// TimerTask can only be scheduled ONCE or PERIODICALLY!
		if (timeout > 0) {
			process_killer = new ProcessKillingTimerTask();
			process_timer.schedule(process_killer, timeout);
		}

		try {
			// Process the standard output; this call is blocking until
			// (i) the process exits NATURALLY;
			// (ii) the process is DESTROYED by the process_killer TimerTask;
			// (iii) the stdout consuming thread is interrupted.
			Streams.processStream(process.getInputStream(), stdout_processor);

			// Time out occurs, raise exception after finally-clause is done!
			if (is_timeout)
				throw new TimeoutException("Timeout while executing "
						+ command[0]);

			// Note: the finally is executed so that the process definitely
			// exits and so we can safely return the exit value
			return process.exitValue();
		} finally {
			// Cancel the scheduled killing to make sure that the next
			// command is not accidentally killed
			if (process_killer != null) {
				process_killer.cancel();
				process_killer = null;
			}
			// Destroy the process if case (iii) happens or exception occurs
			// wait for the process to be completely destroyed and close all
			// resources as well
			kill();
		}
	}

	/**
	 * Destroy the process, wait for it to be completely destroyed and close all
	 * resources (stdin, stdout, stderr streams).
	 */
	private void kill() {
		if (process != null) {
			process.destroy();
			try {
				process.waitFor();
			} catch (Exception e) {
			}
			Streams.closeStream(process.getInputStream());
			Streams.closeStream(process.getOutputStream());
			Streams.closeStream(process.getErrorStream());
		}
	}

}
