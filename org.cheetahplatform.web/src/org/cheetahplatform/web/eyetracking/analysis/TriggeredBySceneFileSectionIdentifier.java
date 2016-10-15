package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.analysis.TrialAnalysisUtil.extractKeysConsideringCollapsedColumns;

import java.util.Set;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

/**
 * Detects a file section based on a fixed scene name; the file section succeeding the fixed name scene is the file section to be
 * identified. This identifier may be used when the name the scene representing a trial/stimulus change, but are always preceded by a scene
 * with a pre-defined name.
 *
 * @author stefan.zugal
 *
 */
public class TriggeredBySceneFileSectionIdentifier extends AbstractPupillometryFileSectionIdentifier {
	/**
	 * The scene of the stimulus, dynamically detected based upon the preceding scene.
	 */
	private String stimulusScene;

	private boolean nextSceneIsStimulusScene;
	/**
	 * The preceding scene to detect.
	 */
	private final String precedingScene;

	public TriggeredBySceneFileSectionIdentifier(String precedingScene) {
		this.precedingScene = precedingScene;
	}

	@Override
	public boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn studioEventDataColumn) {
		// stimulus scene not yet detected --> this is not the end of the stimulus
		if (stimulusScene == null) {
			return false;
		}

		Set<String> studioEvents = extractKeysConsideringCollapsedColumns(line, studioEventDataColumn);
		// no start/event of scene --> this is not the end of the stimulus
		if (studioEvents.isEmpty()) {
			return false;
		}

		stimulusScene = null;
		return true;
	}

	@Override
	public boolean isStart(IPupillometryFileLine line, PupillometryFileColumn studioEventDataColumn) {
		// if the preceding scene has been identified and the stimulus has not been found, try to identify the stimulus
		if (nextSceneIsStimulusScene && stimulusScene == null) {
			Set<String> studioEvents = extractKeysConsideringCollapsedColumns(line, studioEventDataColumn);
			// no start/event of scene --> this is not the start of the stimulus
			if (studioEvents.isEmpty()) {
				return false;
			}

			// the end of the preceding scene
			if (studioEvents.size() == 1 && studioEvents.contains(precedingScene)) {
				return false;
			}

			// otherwise it must be the start of the stimulus scene --> identify the stimulus
			for (String event : studioEvents) {
				if (event.equals(precedingScene)) {
					continue;
				}

				stimulusScene = event;
				nextSceneIsStimulusScene = false;
				break;
			}

			return true;
		}

		// if the current line matches the preceding scene, the next occurring scene must be the stimulus scene
		if (matches(line, studioEventDataColumn, precedingScene)) {
			nextSceneIsStimulusScene = true;
		}

		return false;
	}

}
