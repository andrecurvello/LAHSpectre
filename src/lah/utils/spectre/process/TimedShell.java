package lah.utils.spectre.process;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import lah.utils.spectre.stream.InputBufferProcessor;
import lah.utils.spectre.stream.Streams;

class ProcessWaitingThread extends Thread {

	private final Process proc;

	public ProcessWaitingThread(Process process) {
		proc = process;
	}

	private void kill() {
		if (proc == null)
			return;
		// Kill and wait for the process to be really killed
		try {
			proc.destroy();
			proc.waitFor();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void run() {
		try {
			if (proc != null)
				proc.waitFor();
		} catch (InterruptedException e) {
			// this thread is interrupted, kill the process as in
			// the finally clause
		} finally {
			// this happens when the process terminates naturally
			// or when this thread is interrupted
			// System.out.println("Kill process " + proc);
			kill();
		}
	}

}

/**
 * Shell with command time-out and standard output processing
 * 
 * @author L.A.H.
 * 
 */
public class TimedShell {

	private Process process;

	private final TimerTask process_killer;

	private final Timer process_timer;

	private ProcessWaitingThread waiting_thread;

	public TimedShell() {
		process_timer = new Timer();
		process_killer = new TimerTask() {

			@Override
			public void run() {
				if (waiting_thread != null)
					waiting_thread.interrupt();
			}

		};
	}

	private void closeIOStreams() {
		if (process == null)
			return;
		// Close stdout
		try {
			// System.out.print("Close stdout ... ");
			process.getInputStream().close();
			// System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Close stdin
		try {
			// System.out.print("Close stdin ... ");
			process.getOutputStream().close();
			// System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Close stderr
		try {
			// System.out.print("Close stderr ... ");
			process.getErrorStream().close();
			// System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized int fork(String[] command, File directory,
			InputBufferProcessor processor, long timeout) throws Exception {
		process = new ProcessBuilder(command).directory(directory)
				.redirectErrorStream(true).start();

		// Schedule the timer to time-out the process (if necessary)
		if (timeout > 0)
			process_timer.schedule(process_killer, timeout);

		// Fork a thread to wait for the process to finish WHILE this thread
		// consumes the output: cannot use waitFor() here!
		waiting_thread = new ProcessWaitingThread(process);
		waiting_thread.start();

		try {
			Streams.processStream(process.getInputStream(), processor);
			waiting_thread.join();
			return process.exitValue();
		} catch (Exception e) {
			waiting_thread.interrupt();
			throw e;
		} finally {
			process_killer.cancel();
			closeIOStreams();
		}
	}

}
