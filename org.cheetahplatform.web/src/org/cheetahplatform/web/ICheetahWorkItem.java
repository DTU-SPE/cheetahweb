package org.cheetahplatform.web;

/**
 * Background work items that can be scheduled for execution.
 *
 * @author Jakob
 */
public interface ICheetahWorkItem {

	/**
	 * This method is called when the work item is canceled. Might initialize some clean up.
	 */
	void cancel();

	/**
	 * Do the work!
	 */
	void doWork() throws Exception;

	String getDisplayName();

	long getId();

	long getUserId();

	void setId(long id);
}
