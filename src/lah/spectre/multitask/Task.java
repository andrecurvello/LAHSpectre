package lah.spectre.multitask;

import lah.spectre.interfaces.IResult;

/**
 * General interface for a task
 * 
 * @author L.A.H.
 * 
 */
public interface Task {

	Task getParentTask();

	IResult getResult();

	void start();
	
}
