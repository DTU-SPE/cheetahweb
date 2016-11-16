package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

/**
 * Interface for classes that are able to detect the lines containing to a stimulus within a trial.
 *
 * @author stefan.zugal
 *
 */
public interface IPupillometryFileSectionIdentifier {
	/**
	 * Determines whether the given line represents the end of a stimulus.
	 *
	 * @param line
	 *            the line to be analyzed
	 * @param studioEventDataColumn
	 *            the column containing the data for StudioEventData
	 * @return <code>true</code> if this line represents the end of a stimulus
	 */
	boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn studioEventDataColumn);

	/**
	 * Determines whether the given line represents the start of a stimulus.
	 *
	 * @param line
	 *            the line to be analyzed
	 * @param studioEventDataColumn
	 *            the column containing the data for StudioEventData
	 * @return <code>true</code> if this line represents the start of a stimulus
	 */
	boolean isStart(IPupillometryFileLine line, PupillometryFileColumn studioEventDataColumn);
}
