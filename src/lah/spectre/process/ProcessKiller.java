package lah.spectre.process;

import java.util.TimerTask;

/**
 * Extension of {@link TimerTask} that will kill a process running in background
 * 
 * @author L.A.H.
 * 
 */
class ProcessKiller extends TimerTask {

	/**
	 * Flag to indicate if the process exceeds its time out limit
	 */
	private boolean is_timeout;

	/**
	 * Current executing process
	 */
	private Process process;

	public ProcessKiller(Process process) {
		assert process != null;
		this.process = process;
		this.is_timeout = false;
	}

	public boolean isTimeOut() {
		return is_timeout;
	}

	@Override
	public void run() {
		try {
			// Try to obtain the exit value first, if there is no IllegalThreadStateException raised, then the process
			// already gracefully terminates. In that case, there is actually no timeout.
			process.exitValue();
		} catch (Exception e) {
			// Set time out flag
			is_timeout = true;
			// Destroy the running process; assuming that once the process is destroyed, its standard output reaches EOF
			process.destroy();
			// Note: must not close streams here (after destruction) because we might still be processing the standard
			// output; invoking destroy is sufficient because this makes the stdout stream reach EOF so that invocation
			// of Streams.processStream in TimeShell.fork halts naturally.
		}
	}

}