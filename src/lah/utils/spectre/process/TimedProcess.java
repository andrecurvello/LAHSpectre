package lah.utils.spectre.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import lah.utils.spectre.BuildConfig;
import lah.utils.spectre.CollectionPrinter;
import lah.utils.spectre.interfaces.ExceptionHandler;
import lah.utils.spectre.interfaces.ResultListener;
import lah.utils.spectre.stream.InputBufferProcessor;
import lah.utils.spectre.stream.InputStreamProcessingThread;
import lah.utils.spectre.stream.Streams;

/**
 * This class represents a timed {@link Process}. It has analogous functionality
 * with extra functionality to time the process and kill it if it exceeds the
 * timeout.
 * 
 * @author Vu An Hoa
 * 
 */
public class TimedProcess {

	public static TimedProcess create(String[] command, String[] environment,
			File directory, OutputStream stdout, Timer timer, final long timeout)
			throws IOException, InterruptedException {
		final Process process = Runtime.getRuntime().exec(command, environment,
				directory);

		// Consume the stderr and stdout in background
		// Attempt close the stream to stdout on exit (normally/Exception)
		Streams.pipeIOStreamInBackground(process.getInputStream(), stdout,
				true, true);
		// Just ignore the stderr
		Streams.pipeIOStreamInBackground(process.getErrorStream(), null, false,
				false);

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
			File directory, Timer timer, final long timeout)
			throws IOException, InterruptedException {
		return create(command, environment, directory, null, timer, timeout);
	}

	public static int execute(String[] command, String[] environment,
			File directory, OutputStream stdout, Timer timer, final long timeout)
			throws IOException, InterruptedException {
		TimedProcess timed_process = create(command, environment, directory,
				stdout, timer, timeout);
		Process process = timed_process.getProcess();
		InputStream stdoutstr = process.getInputStream();
		InputStream stderrstr = process.getErrorStream();
		OutputStream stdinstr = process.getOutputStream();
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
			// stdoutstr.close(); // closing here might make the background
			// thread consuming this exit
			stdinstr.close();
			stderrstr.close();
			process.destroy();
			return result;
		} catch (InterruptedException e) {
			if (BuildConfig.DEBUG)
				System.out
						.println("TeX.execTimeOut : Interrupted while waiting for process "
								+ process + " to finish.");
			stdoutstr.close();
			stdinstr.close();
			stderrstr.close();
			process.destroy();
			process.waitFor();
			throw e;
		}
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
			File directory, Timer timer, final long timeout)
			throws IOException, InterruptedException {
		return execute(command, environment, directory, null, timer, timeout);
	}

	// public TimedProcess(String[] command, String[] environment, File
	// directory)
	// throws IOException {
	// process = Runtime.getRuntime().exec(command, environment, directory);
	// }

	private String[] command;

	private Thread output_processing_thread;

	/**
	 * The external process to run
	 */
	private Process process;

	/**
	 * An executing task that will kill the process when time out is reached
	 */
	private TimerTask process_killer;

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
		process_killer = killer;
	}
	
	public TimedProcess() {
	}

	public TimedProcess(String[] command, File directory, boolean redirectError)
			throws IOException {
		this.command = command;
		process = new ProcessBuilder(command).directory(directory)
				.redirectErrorStream(redirectError).start();
	}

	public TimedProcess(String[] command, File directory,
			boolean redirectError, InputBufferProcessor processor,
			ExceptionHandler exception_handler, long timeout)
			throws IOException {
		this(command, directory, redirectError);
		setStdOutHandler(processor, exception_handler, null);
		setAndStartTimeOut(timeout);
	}

	public TimedProcess(String[] command, File directory,
			boolean redirectError, InputBufferProcessor processor, long timeout)
			throws IOException {
		this(command, directory, redirectError);
		setStdOutHandler(processor);
		setAndStartTimeOut(timeout);
	}

	/**
	 * Close the resources allocated for this object and wait for the on-going
	 * output processing thread to finish with the remaining stdout's output
	 * generated by this process.
	 */
	public void destroy() {
		System.out.println("Destroy timed process " + this + " running "
				+ CollectionPrinter.stringOfArray(command, " ", "[", "]"));
		// Wait for the output processing thread to finish
		if (output_processing_thread != null) {
			try {
				output_processing_thread.join();
			} catch (InterruptedException e) {
			}
		}
	}

	public void destroyImmediately() {
		if (process != null) {
			process.destroy();
			try {
				// Wait for the process to be fully killed
				process.waitFor();
				// Close the associated streams
				if (process.getInputStream() != null)
					process.getInputStream().close();
				if (process.getOutputStream() != null)
					process.getOutputStream().close();
				if (process.getErrorStream() != null)
					process.getErrorStream().close();
				if (process_killer != null)
					process_killer.cancel();
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		}
	}

	public Process getProcess() {
		return process;
	}

	public TimerTask getProcessKillingTask() {
		return process_killer;
	}

	public void setAndStartTimeOut(long timeout) {
		if (timeout > 0) {
			process_killer = new TimerTask() {

				@Override
				public void run() {
					destroy();
				}
			};
			new Timer().schedule(process_killer, timeout);
		}
	}

	public void setStdOutHandler(InputBufferProcessor processor) {
		setStdOutHandler(processor, null, null);
	}

	public void setStdOutHandler(InputBufferProcessor processor,
			ExceptionHandler exception_handler,
			ResultListener<Void> result_listener) {
		if (process != null) {
			output_processing_thread = new InputStreamProcessingThread(
					process.getInputStream(), processor, exception_handler,
					result_listener);
			output_processing_thread.start();
		}
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	public int waitForAndDestroy() throws InterruptedException {
		int result = process.waitFor();
		destroy();
		return result;
	}

} // end of TimedProcess