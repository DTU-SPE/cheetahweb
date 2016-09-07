package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;

public interface IEyeTrackingDataAnalyzer {
	/**
	 * Performs the analysis and returns the result.
	 *
	 * @return a list of result lines
	 */
	List<String> analyze() throws Exception;

	String getHeader();

	String getName();

	/**
	 * Returns an identifier for the subject.
	 *
	 * @return the subject's identifier;
	 * @throws Exception
	 */
	String getSubjectIdentifier() throws Exception;
}
