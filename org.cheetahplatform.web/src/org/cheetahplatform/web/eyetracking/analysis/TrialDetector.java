package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class TrialDetector extends AbstractPupillopmetryFileDetector {
	private long fileId;
	private TrialConfiguration config;
	private String decimalSeparator;

	private IPupillometryFileSectionIdentifier trialIdentifier;
	private String timestampColumn;

	public TrialDetector(long fileId, TrialConfiguration config, String decimalSeparator, String timestampColumn) {
		this.fileId = fileId;
		this.config = config;
		this.decimalSeparator = decimalSeparator;
		this.timestampColumn = timestampColumn;
		readConfig();
	}

	private void detectBaseline(PupillometryFileColumn timeStamp, List<Trial> trials) {
		for (Trial trial : trials) {
			BaselineDetector baselineDetector = new BaselineDetector(trial, config, timeStamp);
			baselineDetector.detectBaseline();
		}
	}

	private void detectStimulus(PupillometryFile pupillometryFile, List<Trial> trials) throws Exception {
		for (Trial trial : trials) {
			StimulusDetector stimulusDetector = new StimulusDetector(trial, config, pupillometryFile);
			Stimulus stimulus = stimulusDetector.detectStimulus();
			trial.setStimulus(stimulus);
		}
	}

	public List<Trial> detectTrials() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		String filePath = userFileDao.getPath(fileId);
		File file = userFileDao.getUserFile(filePath);
		PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true, decimalSeparator);
		PupillometryFileColumn timeStamp = pupillometryFile.getHeader().getColumn(timestampColumn);
		pupillometryFile.collapseEmptyColumns(timeStamp);

		List<Trial> trials = findTrials(pupillometryFile);
		detectStimulus(pupillometryFile, trials);
		detectBaseline(timeStamp, trials);

		return trials;
	}

	private List<Trial> findTrials(PupillometryFile pupillometryFile) throws Exception {
		List<Trial> trials = new ArrayList<>();

		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);

		List<PupillometryFileLine> lines = pupillometryFile.getContent();
		Trial currentTrial = null;
		String previousScene = "";
		for (PupillometryFileLine line : lines) {
			if (currentTrial != null) {
				currentTrial.addLine(line);
			}

			List<PupillometryFileLine> linesToCheck = extractLinesToConsider(line);
			for (PupillometryFileLine pupillometryFileLine : linesToCheck) {
				String scene = pupillometryFileLine.get(studioEventDataColumn);
				if (scene == null || scene.trim().isEmpty() || previousScene.equals(scene)) {
					continue;
				}

				if (trialIdentifier.isEnd(pupillometryFileLine, studioEventDataColumn)) {
					currentTrial = null;
				}

				if (trialIdentifier.isStart(pupillometryFileLine, studioEventDataColumn)) {
					if (currentTrial != null) {
						throw new IllegalStateException("A trial can only start after the previous trial has ended.");
					}
					currentTrial = new Trial();
					trials.add(currentTrial);
				}

				previousScene = scene;
			}
		}
		return trials;
	}

	private void readConfig() {
		String trialStart = config.getTrialStart();
		String trialEnd = null;
		if (config.isUseTrialStartForTrialEnd()) {
			trialEnd = trialStart;
		} else {
			trialEnd = config.getTrialEnd();
		}

		trialIdentifier = new StartAndEndPupillometryFileSectionIdentifier(trialStart, trialEnd);
	}
}
