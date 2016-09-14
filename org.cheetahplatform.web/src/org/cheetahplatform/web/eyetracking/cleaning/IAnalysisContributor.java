package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

/**
 * Interface for classes specializing analysis.
 *
 * @author stefan.zugal
 *
 */
public interface IAnalysisContributor {
	String SCENE_ENDED = "SceneEnded";
	String SCENE_STARTED = "SceneStarted";

	/**
	 * Aggregates the studio events to trials.
	 *
	 * @param file
	 *            the file to be processed
	 * @param trialColumn
	 *            the column in which the trial id should be stored
	 * @param studioEventColumn
	 *            column containing the studio event
	 * @param studioEventDataColumn
	 *            column containing the studio event data
	 * @throws IOException
	 */
	void computeTrialColumn(PupillometryFile file, PupillometryFileColumn trialColumn, PupillometryFileColumn studioEventColumn,
			PupillometryFileColumn studioEventDataColumn) throws IOException;

	/**
	 * Determines whether the file indicates the end of a scene.
	 *
	 * @param file
	 *            the file currently being processed
	 * @param iterator
	 *            iterator of the current analysis process. If you change the iterator, make sure to reset it.
	 * @param line
	 *            the line being processed
	 * @return <code>true</code> if the given line indicates the end of a scene, <code>false</code> if not
	 */
	boolean isSceneEnd(PupillometryFile file, ListIterator<PupillometryFileLine> iterator, PupillometryFileLine line) throws IOException;

	/**
	 * Determines whether the given line indicates the start of a scene.
	 *
	 * @param file
	 *            the file that is currently being processed
	 * @param iterator
	 *            iterator of the current analysis process. If you change the iterator, make sure to reset it.
	 * @param line
	 *            the line to be processed
	 * @return <code>true</code> if the line represents the start of a scene, <code>false</code> if not
	 */
	boolean isSceneStart(PupillometryFile file, ListIterator<PupillometryFileLine> iterator, PupillometryFileLine line) throws IOException;

	/**
	 * Processes the scene columns - copies the content from the StudioEvent and StudioEventData to the scene column.
	 *
	 * @param content
	 *            the content to be processed
	 * @param sceneColumn
	 *            column in which the scene should be stored
	 * @param eventColumn
	 *            column containing the studio event
	 * @param eventDataColumn
	 *            column containing the studio event data
	 */
	void processSceneColumns(List<PupillometryFileLine> content, PupillometryFileColumn sceneColumn, PupillometryFileColumn eventColumn,
			PupillometryFileColumn eventDataColumn);
}
