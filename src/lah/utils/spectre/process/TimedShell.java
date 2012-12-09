package lah.utils.spectre.process;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import lah.utils.spectre.interfaces.ExceptionHandler;
import lah.utils.spectre.stream.InputBufferProcessor;
import lah.utils.spectre.stream.InputStreamProcessingThread;

public class TimedShell {

	/**
	 * Global time out for all process; 0 means NO timeout
	 */
	@SuppressWarnings("unused")
	private long global_time_out = 0;

	private Process current_process;

	private Thread stdout_processing_thread;

	private TimerTask kill_process_task;

	private Timer process_timer;

	public TimedShell() {
		process_timer = new Timer();
	}

	public void setGlobalTimeOut(long timeout) {
		global_time_out = timeout;
	}

	public int invoke(String[] command, File directory,
			InputBufferProcessor processor, long timeout) throws IOException {
		// Destroy the running process (if any) and create a new one
		destroyCurrentProcess();
		current_process = new ProcessBuilder(command).directory(directory)
				.redirectErrorStream(true).start();

		// Set up the thread to consume standard output
		stdout_processing_thread = new InputStreamProcessingThread(
				current_process.getInputStream(), processor,
				new ExceptionHandler() {

					@Override
					public void onException(Exception e) {
						destroyCurrentProcess();
					}
				}, null);
		stdout_processing_thread.start();

		// Set up the timer to time-out the process if necessary
		if (timeout > 0) {
			kill_process_task = new TimerTask() {

				@Override
				public void run() {
					destroyCurrentProcess();
				}
			};
			process_timer.schedule(kill_process_task, timeout);
		}

		// Now wait for result or exception
		try {
			int result = current_process.waitFor();
			destroyCurrentProcess();
			return result;
		} catch (InterruptedException e) {
			destroyCurrentProcess();
			return -1;
		}
	}

	private void destroyCurrentProcess() {
		if (current_process != null) {
			current_process.destroy();
			try {
				// Wait for the process to be fully killed
				current_process.waitFor();
				// Close the associated streams
				if (current_process.getInputStream() != null)
					current_process.getInputStream().close();
				if (current_process.getOutputStream() != null)
					current_process.getOutputStream().close();
				if (current_process.getErrorStream() != null)
					current_process.getErrorStream().close();
				if (kill_process_task != null)
					kill_process_task.cancel();
			} catch (InterruptedException e) {
				
			} catch (IOException e) {
				
			}
		}
	}

}
