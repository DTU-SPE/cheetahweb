package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Map;

import org.cheetahplatform.web.dto.ReportableResult;

/**
 * An interface for writers that are capable of writing result files.
 *
 * @author Jakob
 *
 */
public interface IReportableResultWriter {

	/**
	 * Write the result to disk.
	 *
	 * @param userId
	 *            the user's id
	 * @param collectedResults
	 *            the collected {@link ReportableResult}s
	 * @param filePrefix
	 *            a prefix for the file
	 * @param resultFileComment
	 *            the comment for the file
	 */
	void write(long userId, Map<String, ReportableResult> collectedResults, String filePrefix, String resultFileComment);

}