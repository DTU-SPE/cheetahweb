package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class StimulusDetector extends AbstractPupillopmetryFileDetector {
	private AbstractPupillometryFileSection trial;
	private TrialConfiguration config;
	private PupillometryFile pupillometryFile;
	private IPupillometryFileSectionIdentifier stimulusIdentifier;

	public StimulusDetector(AbstractPupillometryFileSection trial, TrialConfiguration config, PupillometryFile pupillometryFile) {
		this.trial = trial;
		this.config = config;
		this.pupillometryFile = pupillometryFile;

		readConfiguration();
	}

	public Stimulus detectStimulus() throws Exception {
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);

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
						throw new IllegalStateException("Found multiple stimulus start events in one trial.");
					}
					stimulus = new Stimulus();
				}

				previousScene = scene;
			}
		}

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
