package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Experiment contributor for Thomas Marans first experiment.
 *
 * @author stefan.zugal
 *
 */
public class ThomasMaran1Contributor implements IAnalysisContributor {

	public static final String SCENE_ENDED = "SceneEnded";
	public static final String SCENE_STARTED = "SceneStarted";

	@SuppressWarnings("unchecked")
	@Override
	public void computeTrialColumn(PupillometryFile file, PupillometryFileColumn trialColumn, PupillometryFileColumn studioEventColumn,
			PupillometryFileColumn studioEventDataColumn) throws IOException {
		int trialId = 0;

		for (PupillometryFileLine line : file.getContent()) {
			List<PupillometryFileLine> collapsed = (List<PupillometryFileLine>) line.getMarking(PupillometryFile.COLLAPSED_COLUMNS);

			if (collapsed != null) {
				for (PupillometryFileLine collapsedLine : collapsed) {
					String event = collapsedLine.get(studioEventColumn);
					String currentEvent = collapsedLine.get(studioEventDataColumn);

					// trials always start with "Fixation XYZ"
					if (SCENE_STARTED.equals(event) && currentEvent.contains("Fixation")) {
						trialId++;
					}
				}
			}

			line.setValue(trialColumn, trialId);
		}
	}

	@Override
	public boolean isSceneEnd(PupillometryFile file, ListIterator<PupillometryFileLine> iterator, PupillometryFileLine line)
			throws IOException {
		return isSceneOfType(file, line, SCENE_ENDED);
	}

	@SuppressWarnings("unchecked")
	private boolean isSceneOfType(PupillometryFile file, PupillometryFileLine line, String type) throws IOException {
		List<PupillometryFileLine> collapsed = (List<PupillometryFileLine>) line.getMarking(PupillometryFile.COLLAPSED_COLUMNS);
		PupillometryFileColumn studioEventColumn = file.getHeader().getColumn(CleanPupillometryDataWorkItem.STUDIO_EVENT);

		if (collapsed != null) {
			for (PupillometryFileLine collapsedLine : collapsed) {
				String event = collapsedLine.get(studioEventColumn);
				if (type.equals(event)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isSceneStart(PupillometryFile file, ListIterator<PupillometryFileLine> iterator, PupillometryFileLine line)
			throws IOException {
		return isSceneOfType(file, line, SCENE_STARTED);
	}

	@Override
	public void processSceneColumns(List<PupillometryFileLine> content, PupillometryFileColumn sceneColumn,
			PupillometryFileColumn eventColumn, PupillometryFileColumn eventDataColumn) {
		String currentScene = "";

		Iterator<PupillometryFileLine> iterator = content.iterator();
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			String event = current.get(eventColumn);
			if (event.equals("SceneStarted")) {
				currentScene = current.get(eventDataColumn);
			} else if (event.equals("SceneEnded")) {
				currentScene = "";
			}

			current.setValue(sceneColumn, currentScene);
		}
	}
}
