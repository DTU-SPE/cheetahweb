package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Experiment contributor for Thomas Marans Persona Experiment (see #311)
 *
 * @author stefan.zugal
 *
 */
public class ThomasMaranPersonaExperimentContributor implements IAnalysisContributor {

	private static final String IMAGE_START = "ImageStart";
	private static final String INSTRUCTION_START = "InstructionStart";
	private static final String BLANK_JPG = "Blank.jpg";

	@Override
	public void computeTrialColumn(PupillometryFile file, PupillometryFileColumn trialColumn, PupillometryFileColumn studioEventColumn,
			PupillometryFileColumn studioEventDataColumn) throws IOException {
		int trialId = 0;
		String previousScene = null;
		PupillometryFileColumn sceneColumn = file.getHeader().getColumn(PupillometryFile.SCENE);

		for (PupillometryFileLine line : file.getContent()) {
			String currentScene = line.get(sceneColumn);

			// ignore the instructions when computing the trials
			if (currentScene.trim().isEmpty()) {
				continue;
			}

			// One trial are several BLANK_JPGS followed by several images (e.g., 503.bmp). Increase trial number whenever the event
			// changes from BLANK_JPG to an image
			if (previousScene == null || (!previousScene.equals(currentScene) && currentScene.equals(BLANK_JPG))) {
				trialId++;
			}
			line.setValue(trialColumn, trialId);
			previousScene = currentScene;
		}
	}

	@Override
	public boolean isSceneEnd(PupillometryFile file, ListIterator<PupillometryFileLine> iterator, PupillometryFileLine line)
			throws IOException {
		PupillometryFileColumn sceneColumn = file.getHeader().getColumn(PupillometryFile.SCENE);
		if (!iterator.hasNext()) {
			return false;
		}

		String nextScene = iterator.next().get(sceneColumn);
		iterator.previous();
		String currentScene = line.get(sceneColumn);
		return !currentScene.equals(nextScene) && !currentScene.trim().isEmpty();
	}

	@Override
	public boolean isSceneStart(PupillometryFile file, ListIterator<PupillometryFileLine> iterator, PupillometryFileLine line)
			throws IOException {
		PupillometryFileColumn sceneColumn = file.getHeader().getColumn(PupillometryFile.SCENE);
		if (iterator.previousIndex() <= 0) {
			return false;
		}

		// need to invoke previous twice --> first call returns the same result (see javadoc of previous())
		iterator.previous();
		String previousScene = iterator.previous().get(sceneColumn);
		iterator.next();
		iterator.next();
		String currentScene = line.get(sceneColumn);
		return !currentScene.equals(previousScene) && !currentScene.trim().isEmpty();
	}

	@Override
	public void processSceneColumns(List<PupillometryFileLine> content, PupillometryFileColumn sceneColumn,
			PupillometryFileColumn eventColumn, PupillometryFileColumn eventDataColumn) {
		String currentScene = "";

		Iterator<PupillometryFileLine> iterator = content.iterator();
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			String event = current.get(eventColumn);
			if (event.equals(IMAGE_START)) {
				currentScene = current.get(eventDataColumn);
			} else if (event.equals(INSTRUCTION_START)) {
				currentScene = "";
			}

			current.setValue(sceneColumn, currentScene);
		}
	}

}
