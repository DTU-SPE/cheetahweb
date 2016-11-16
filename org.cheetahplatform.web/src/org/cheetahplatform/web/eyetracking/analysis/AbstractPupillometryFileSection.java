package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class AbstractPupillometryFileSection {
	protected List<PupillometryFileLine> lines;

	public AbstractPupillometryFileSection() {
		super();
		lines = new ArrayList<>();
	}

	public void addLine(PupillometryFileLine line) {
		lines.add(line);
	}

	/**
	 * Computes the scenes that are contained in this section.
	 *
	 * @param studioEventDataColumn
	 *            the column describing StudioEvent in the lines contained in this section
	 * @param studioEventDataColumn
	 *            the column describing StudioEventData in the lines contained in this section
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> computeScenes(PupillometryFileColumn studioEventColumn, PupillometryFileColumn studioEventDataColumn) {
		List<String> scenes = new ArrayList<>();
		String previousScene = null;

		// scenes always start with xxxStart (where xxx may be something like Image, Question, Video and the like) and end with xxxEnd.
		// Since the STUDIO_EVENT_DATA data is always added to the previous pupillometry line, the first xxxSTART event is not added to the
		// trial --> we use the xxxEnd events to determining the scenes.
		for (PupillometryFileLine line : lines) {
			List<PupillometryFileLine> collapsedLines = (List<PupillometryFileLine>) line.getMarking(PupillometryFile.COLLAPSED_COLUMNS);
			if (collapsedLines != null) {
				for (PupillometryFileLine collapsedLine : collapsedLines) {
					String currentScene = collapsedLine.get(studioEventDataColumn);
					String event = collapsedLine.get(studioEventColumn);
					if (event != null && (event.endsWith("End") || event.endsWith("Ended"))) {
						if (currentScene != null && !currentScene.equals(previousScene)) {
							scenes.add(currentScene);
							previousScene = currentScene;
						}
					}
				}
			}
		}

		return scenes;
	}

	public List<PupillometryFileLine> getLines() {
		return Collections.unmodifiableList(lines);
	}
}