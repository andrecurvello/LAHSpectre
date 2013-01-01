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

	@Override
	public void run() {
		try {
			if (proc != null)
				proc.waitFor();
		} catch (InterruptedException e) {
			// how to mark exception?
		} finally {
			// Kill and wait for the process to be really killed
			// in any situation: when the process terminates naturally
			// or when this waiting thread is interrupted
			if (proc != null) {
				try {
					proc.destroy();
					proc.waitFor();
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
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

	private TimerTask process_killer;

	private final Timer process_timer = new Timer();

	private ProcessWaitingThread waiting_thread;

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
		if (timeout > 0) {
			// Have to recreate the TimerTask again and again since TimerTask
			// can only be scheduled ONCE!
			process_killer = new TimerTask() {

				@Override
				public void run() {
					if (waiting_thread != null)
						waiting_thread.interrupt();
				}

			};
			process_timer.schedule(process_killer, timeout);
		}

		// Fork a thread to wait for the process to finish WHILE this thread
		// consumes the output: cannot use waitFor() here AND the timer is
		// running another thread timing out the execution.
		waiting_thread = new ProcessWaitingThread(process);
		waiting_thread.start();

		try {
			// process the stdout, note that in case of time out, the waiting
			// thread is interrupted and so the process be abruptly killed and
			// we assume that when the process is killed, EOF on stdout is
			// reached so that this eventually terminates (when timeout > 0).
			Streams.processStream(process.getInputStream(), processor);

			// finish processing stdout and stdout is closed so wait for the
			// waiting thread to finish destruction of the process object before
			// returning its exit value
			waiting_thread.join();

			// do the finally and then return the exit value
			return process.exitValue();
		} catch (Exception e) {
			// exception while processing stdout, interrupt the waiting thread
			// so that it kills the running process; and then we wait for kill
			// to finish
			waiting_thread.interrupt();
			waiting_thread.join();

			// do finally before throwing exception to caller
			throw e;
		} finally {
			// before return or raising exception, cancel the scheduled killing
			if (process_killer != null)
				process_killer.cancel();
			process_killer = null;

			// close all open resources
			closeIOStreams();
		}
	}

}
