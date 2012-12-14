package lah.utils.spectre.process;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import lah.utils.spectre.interfaces.ExceptionHandler;
import lah.utils.spectre.stream.InputBufferProcessor;
import lah.utils.spectre.stream.InputStreamProcessingThread;

/**
 * This class extends {@link TimedProcess} to preserve space.
 * 
 * @author L.A.H.
 * 
 */
public class TimedShell extends TimedProcess {

	/**
	 * Global time out for all process; 0 means NO timeout
	 */
	private long global_time_out = 0;

	/**
	 * Global object to handle exceptions
	 */
	private final ExceptionHandler global_exception_handler = new ExceptionHandler() {

		@Override
		public void onException(Exception e) {
			// Simply kill the process if exception is encountered
			kill();
		}

	};

	/**
	 * Global timer to time out the process
	 */
	private Timer process_timer;

	public TimedShell() {
		super();
		process_timer = new Timer();
		process_killer = new TimerTask() {

			@Override
			public void run() {
				destroy();
			}

		};
	}

	public void setGlobalTimeOut(long timeout) {
		global_time_out = timeout;
	}

	public synchronized int invoke(String[] command, File directory,
			InputBufferProcessor processor, long timeout) throws IOException,
			InterruptedException {
		// Destroy the running process (if any)
		kill();
		// And create a new one to run the command
		process = new ProcessBuilder(command).directory(directory)
				.redirectErrorStream(true).start();

		// Set up the thread to consume standard output
		stdout_processing_thread = new InputStreamProcessingThread(
				process.getInputStream(), processor, global_exception_handler,
				null);
		stdout_processing_thread.start();

		// Schedule the timer to time-out the process (if necessary)
		timeout = (timeout == 0 ? global_time_out : timeout);
		if (timeout > 0)
			process_timer.schedule(process_killer, timeout);

		// Now wait for result | exception to be thrown
		return waitForAndDestroy();
	}

}
