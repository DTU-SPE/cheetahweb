package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class StimulusDetector extends AbstractPupillopmetryFileDetector {
	private Trial trial;
	private TrialConfiguration config;
	private PupillometryFile pupillometryFile;
	private IPupillometryFileSectionIdentifier stimulusIdentifier;

	public StimulusDetector(Trial trial, TrialConfiguration config, PupillometryFile pupillometryFile) {
		this.trial = trial;
		this.config = config;
		this.pupillometryFile = pupillometryFile;

		readConfiguration();
	}

	public Stimulus detectStimulus() throws Exception {
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);
		PupillometryFileColumn studioEventColumn = pupillometryFile.getHeader().getColumn(CleanPupillometryDataWorkItem.STUDIO_EVENT);

		String previousScene = "";
		Stimulus stimulus = null;
		for (PupillometryFileLine line : trial.getLines()) {
			if (stimulus != null) {
				stimulus.addLine(line);
			}

			List<PupillometryFileLine> extractLinesToConsider = extractLinesToConsider(line);
			for (PupillometryFileLine pupillometryFileLine : extractLinesToConsider) {
				String scene = pupillometryFileLine.get(studioEventDataColumn);
				if (previousScene.equals(scene) || scene == null || scene.trim().isEmpty()) {
					continue;
				}
				if (stimulusIdentifier.isEnd(pupillometryFileLine, studioEventDataColumn)) {
					return stimulus;
				}

				if (stimulusIdentifier.isStart(pupillometryFileLine, studioEventDataColumn)) {
					if (stimulus != null) {
						logErrorNotifcation("Found multiple stimulus start events in one trial.");
						return null;
					}
					stimulus = new Stimulus();
				}

				previousScene = scene;
			}
		}

		if (stimulus == null) {
			logWarningNotifcation("We failed to identify a stimulus in trial " + trial.getTrialNumber() + ".");
		} else {
			List<String> stimulusScenes = stimulus.computeScenes(studioEventColumn, studioEventDataColumn);
			if (stimulusScenes.size() > 1) {
				logWarningNotifcation("The stimulus in trial " + trial.getTrialNumber()
						+ " contains more than one scene. Are you sure the configuration is correct?");
			}
		}

		trial.addAllNotifications(getNotifications());
		return stimulus;
	}

	private void readConfiguration() {
		StimulusConfiguration rawStimulus = config.getStimulus();

		if (rawStimulus instanceof DefaultStimulusConfiguration) {
			DefaultStimulusConfiguration stimulus = (DefaultStimulusConfiguration) rawStimulus;
			String stimulusStart = stimulus.getStimulusStart();
			String stimulusEnd = null;
			if (stimulus.isStimulusEndsWithTrialEnd()) {
				stimulusIdentifier = new StartPupillometryFileSectionIdentifier(stimulusStart);
			} else {
				stimulusIdentifier = new StartAndEndPupillometryFileSectionIdentifier(stimulusStart, stimulusEnd);
			}
		} else if (rawStimulus instanceof StimulusTriggeredByPreviousScene) {
			StimulusTriggeredByPreviousScene stimulus = (StimulusTriggeredByPreviousScene) rawStimulus;
			stimulusIdentifier = new TriggeredBySceneFileSectionIdentifier(stimulus.getPrecedesStimulus());
		} else {
			throw new RuntimeException("Unknown stimulus detection: " + rawStimulus.getClass());
		}
	}
}
