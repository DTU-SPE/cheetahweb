package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class StimulusDetector extends AbstractPupillopmetryFileDetector {
	private static final String STIMULUS_COLUMN_NAME = "Stimulus";
	private static final String TIME_SINCE_STIMULUS_START = "Time_since_stimulus_start";
	private Trial trial;
	private TrialConfiguration config;
	private PupillometryFile pupillometryFile;
	private IPupillometryFileSectionIdentifier stimulusIdentifier;
	private PupillometryFileColumn timeStampColumn;

	public StimulusDetector(Trial trial, TrialConfiguration config, PupillometryFile pupillometryFile, PupillometryFileColumn timeStamp) {
		this.trial = trial;
		this.config = config;
		this.pupillometryFile = pupillometryFile;
		this.timeStampColumn = timeStamp;

		readConfiguration();
	}

	public Stimulus detectStimulus() throws Exception {
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);
		PupillometryFileColumn studioEventColumn = pupillometryFile.getHeader().getColumn(CleanPupillometryDataWorkItem.STUDIO_EVENT);

		PupillometryFileColumn baseLineColumn = initializeColumn(pupillometryFile, STIMULUS_COLUMN_NAME);
		PupillometryFileColumn relativeTimeColumn = initializeColumn(pupillometryFile, TIME_SINCE_STIMULUS_START);

		String previousScene = "";
		Stimulus stimulus = null;
		boolean stimulusComplete = false;
		for (PupillometryFileLine line : trial.getLines()) {
			List<PupillometryFileLine> extractLinesToConsider = extractLinesToConsider(line);
			for (PupillometryFileLine pupillometryFileLine : extractLinesToConsider) {
				String scene = pupillometryFileLine.get(studioEventDataColumn);
				if (previousScene.equals(scene) || scene == null || scene.trim().isEmpty()) {
					continue;
				}
				if (stimulusIdentifier.isEnd(pupillometryFileLine, studioEventDataColumn)) {
					stimulusComplete = true;
					break;
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

			if (stimulus != null) {
				line.setValue(baseLineColumn, Boolean.TRUE.toString());
				addRelativeTime(relativeTimeColumn, timeStampColumn, stimulus.getLines(), line);

				stimulus.addLine(line);
			}

			if (stimulusComplete) {
				break;
			}
		}

		if (stimulus == null) {
			logWarningNotifcation("We failed to identify a stimulus in trial " + trial.getTrialNumber() + ".");
		} else {
			List<String> stimulusScenes = stimulus.computeScenes(studioEventColumn, studioEventDataColumn);
			if (stimulusScenes.size() > 1) {
				logInfoNotifcation("The stimulus in trial " + trial.getTrialNumber()
						+ " contains more than one scene. Are you sure the configuration is correct?");
			}
		}
		return stimulus;
	}

	private void readConfiguration() {
		StimulusConfiguration rawStimulus = config.getStimulus();

		if (rawStimulus instanceof DefaultStimulusConfiguration) {
			DefaultStimulusConfiguration defaultConfiguration = (DefaultStimulusConfiguration) rawStimulus;
			String stimulusStart = defaultConfiguration.getStimulusStart();
			if (defaultConfiguration.isStimulusEndsWithTrialEnd()) {
				stimulusIdentifier = new StartPupillometryFileSectionIdentifier(stimulusStart);
			} else {
				stimulusIdentifier = new StartAndEndPupillometryFileSectionIdentifier(stimulusStart, defaultConfiguration.getStimulusEnd());
			}
		} else if (rawStimulus instanceof StimulusTriggeredByPreviousScene) {
			StimulusTriggeredByPreviousScene stimulus = (StimulusTriggeredByPreviousScene) rawStimulus;
			stimulusIdentifier = new TriggeredBySceneFileSectionIdentifier(stimulus.getPrecedesStimulus());
		} else {
			throw new RuntimeException("Unknown stimulus detection: " + rawStimulus.getClass());
		}
	}
}
