package lah.spectre.process;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

import lah.spectre.BuildConfig;
import lah.spectre.Collections;
import lah.spectre.stream.IBufferProcessor;
import lah.spectre.stream.Streams;

/**
 * Shell with command time-out and standard output processing
 * 
 * @author L.A.H.
 * 
 */
public class TimedShell {

	/**
	 * Destroy the process, wait for it to be completely destroyed and close all resources (stdin, stdout, stderr
	 * streams).
	 * 
	 * @param process
	 *            The process to kill
	 * @return Exit value of process or dummy value 0 in case of exception
	 */
	public static int kill(Process process) {
		if (process != null) {
			process.destroy();
			try {
				return process.waitFor();
			} catch (Exception e) {
				return 0;
			} finally {
				Streams.closeStream(process.getInputStream());
				Streams.closeStream(process.getOutputStream());
				Streams.closeStream(process.getErrorStream());
			}
		}
		return 0;
	}

	/**
	 * Map to store exported environment
	 */
	private Map<String, String> global_environment = new HashMap<String, String>();

	/**
	 * Global timer to timeout the external process
	 */
	private final Timer process_timer = new Timer();

	/**
	 * Set the value of a environment variable for ALL subsequent fork
	 * 
	 * @param env_variable
	 * @param value
	 */
	public void export(String env_variable, String value) {
		if (env_variable != null && value != null)
			global_environment.put(env_variable, value);
	}

	/**
	 * Execute the command with {@literal null} stdout processor and no time out
	 * 
	 * @param command
	 *            the command to execute
	 * @param directory
	 *            the working directory to run
	 * @return exit value of the command
	 * @throws Exception
	 *             should be either {@link IOException} or {@link TimeoutException}
	 */
	public int fork(String[] command, File directory) throws Exception {
		return fork(command, directory, null, 0);
	}

	public int fork(String[] command, File directory, IBufferProcessor stdout_processor, long timeout) throws Exception {
		return fork(command, directory, null, stdout_processor, null, timeout);
	}

	/**
	 * Fork a new process to interact with a command
	 * 
	 * IMPORTANT REMARK: Make sure that the standard output processor is NOT IN USED if it has some STATE.
	 * 
	 * @param command
	 *            The (tokenized) command to run
	 * @param directory
	 *            The working directory to run the command
	 * @param extra_environment
	 *            The additional environment or modification of existing value where the environment variables are at
	 *            even positions and their values are at odd positions (i.e. the value of the environment variable
	 *            extra_environment[2*n] will be set to extra_environment[2*n+1]). Note that we expect the length of the
	 *            array to be even!
	 * 
	 *            The process to be created will execute with the current system environment if this input is
	 *            {@code null}
	 * @param stdout_processor
	 *            Object to process the standard output, if this input is {@literal null}, the output is simply ignored
	 *            (the effect is similar to sending to /dev/null)
	 * @param stdin_producer
	 *            Object to interact with the external process
	 * @param timeout
	 *            Maximum allowable time for the process to execute if greater than 0; input 0 means no timing or
	 *            unlimited allowance
	 * @return The exit value of the executed command
	 * @throws Exception
	 *             {@link TimeoutException} if the timeout is reached and the process has not finished; or any exception
	 *             raised by <b>stdout_processor</b> while processing the standard output.
	 *             {@link ArrayIndexOutOfBoundsException} if the environment array is of odd length.
	 */
	public int fork(String[] command, File directory, String[] extra_environment, IBufferProcessor stdout_processor,
			IBufferProcessor stdin_producer, long timeout) throws Exception {
		// 1. Create the process
		Process process;
		try {
			ProcessBuilder proc_builder = new ProcessBuilder(command).directory(directory).redirectErrorStream(true);
			// Set up the environment for the process
			Map<String, String> env = proc_builder.environment();
			// Set the global (exported) variables
			for (Entry<String, String> e : global_environment.entrySet())
				env.put(e.getKey(), e.getValue());
			// Set the extra variables
			if (extra_environment != null) {
				for (int i = 0; i < extra_environment.length; i += 2)
					env.put(extra_environment[i], extra_environment[i + 1]);
			}
			if (BuildConfig.DEBUG) {
				System.out.println("TimedShell: execute " + Collections.stringOfArray(command, ",", "[", "]") + " @ "
						+ directory.getAbsolutePath() + " with environment");
				for (Entry<String, String> e : proc_builder.environment().entrySet()) {
					System.out.println(e.getKey() + " = " + e.getValue());
				}
			}
			// Start the new process
			process = proc_builder.start();
		} catch (UnsupportedOperationException exception) {
			// Cannot build the environment, try an alternative using Runtime
			String[] env = new String[global_environment.size()
					+ (extra_environment == null ? 0 : extra_environment.length / 2)];
			int i = 0;
			for (Entry<String, String> e : global_environment.entrySet())
				env[i++] = e.getKey() + "=" + e.getValue();
			if (extra_environment != null) {
				for (int j = 0; j < extra_environment.length; j += 2)
					env[i++] = extra_environment[j] + "=" + extra_environment[j + 1];
			}
			process = Runtime.getRuntime().exec(command, env, directory);
			// TODO redirect error stream!?
		}

		// 2. Schedule the timer to time-out the process (if necessary)
		// Note that we have to recreate the TimerTask again and again since TimerTask can only be scheduled ONCE or
		// repeated PERIODICALLY!
		ProcessKiller process_killer = null;
		if (timeout > 0) {
			process_killer = new ProcessKiller(process);
			process_timer.schedule(process_killer, timeout);
		}

		// 3. Process generated standard output
		try {
			// Note: the following call is blocking until (i) the process exits NATURALLY; (ii) the process is DESTROYED
			// by the process_killer object; (iii) the stdout consuming (i.e. current) thread is interrupted.
			Streams.processStream(stdout_processor, process.getInputStream());
		} finally {
			// Cancel the scheduled killing if applicable
			if (process_killer != null)
				process_killer.cancel();
			// Destroy the process if case (iii) happens or exception occurs wait for the process to be completely
			// destroyed and close all resources as well
			kill(process);
			// Note: this finally block is presumably always executed so that the process definitely exits and so we can
			// safely invoke process.exitValue() on the process afterward
		}

		// 4. Return exit value
		if (process_killer != null && process_killer.isTimeOut())
			// Time out occurs, raise exception
			throw new TimeoutException("Timeout while executing " + command[0]);
		else
			return process.exitValue();
	}

	public int fork(String[] command, File directory, String[] extra_environment, IBufferProcessor stdout_processor,
			long timeout) throws Exception {
		return fork(command, directory, extra_environment, stdout_processor, null, timeout);
	}

	public String getEnv(String variable) {
		if (variable == null)
			return null;
		return (global_environment.containsKey(variable)) ? global_environment.get(variable) : System.getenv(variable);
	}

}
