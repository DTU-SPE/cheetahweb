package org.cheetahplatform.web.eyetracking.cleaning;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;

/**
 * An interface for {@link AbstractCheetahWorkItem}s that can be run as part of a {@link DataProcessing} routine.
 * 
 * @author Jakob
 *
 */
public interface IDataProcessingWorkItem {
	/**
	 * Runs the data processing routine on the given {@link PupillometryFile}.
	 * 
	 * @param file
	 *            the {@link PupillometryFile}
	 * @param context
	 *            The {@link DataProcessingContext} that has to be populated by the implementing {@link IDataProcessingWorkItem}
	 * @return <code>true</code> if the work item was executed successfully, <code>false</code> otherwise.
	 * @throws Exception
	 *             if something goes wrong
	 */
	public boolean doWork(PupillometryFile file, DataProcessingContext context) throws Exception;
}
