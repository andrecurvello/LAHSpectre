package lah.spectre.multitask;

import java.util.concurrent.Future;

import lah.spectre.interfaces.IResult;

/**
 * General interface for a task
 * 
 * @author L.A.H.
 * 
 */
public interface Task {

	IResult getResult();

	void setFuture(Future<?> future);

	void start();

}
