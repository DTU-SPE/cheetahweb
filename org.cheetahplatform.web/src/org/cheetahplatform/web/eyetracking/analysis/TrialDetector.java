package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
		super();
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
			trial.addAllNotifications(stimulusDetector.getNotifications());
		}
	}

	public TrialEvaluation detectTrials() throws Exception {
		return detectTrials(true, true);
	}

	public TrialEvaluation detectTrials(boolean detectStimulus, boolean detectBaseline) throws Exception {
		PupillometryFile pupillometryFile = loadPupillometryFile();

		List<Trial> trials = splitFileIntoTrials(pupillometryFile);
		if (trials.isEmpty()) {
			return new TrialEvaluation(getNotifications());
		}

		if (detectStimulus) {
			detectStimulus(pupillometryFile, trials);
		}
		if (detectBaseline) {
			detectBaseline(pupillometryFile.getHeader().getColumn(timestampColumn), trials);
		}

		return new TrialEvaluation(trials, getNotifications());
	}

	/**
	 * Loads the pupillometry file defined in this detector.
	 *
	 * @return
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PupillometryFile loadPupillometryFile() throws SQLException, FileNotFoundException, IOException {
		UserFileDao userFileDao = new UserFileDao();
		String filePath = userFileDao.getPath(fileId);
		File file = userFileDao.getUserFile(filePath);
		PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true, decimalSeparator);
		PupillometryFileColumn timeStamp = pupillometryFile.getHeader().getColumn(timestampColumn);
		pupillometryFile.collapseEmptyColumns(timeStamp);
		return pupillometryFile;
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

	/**
	 * Splits the pupillometry file into trials (does not compute the stimuli and baseline).
	 *
	 * @param pupillometryFile
	 * @return
	 * @throws Exception
	 */
	public List<Trial> splitFileIntoTrials(PupillometryFile pupillometryFile) throws Exception {
		List<Trial> trials = new ArrayList<>();

		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);

		List<PupillometryFileLine> lines = pupillometryFile.getContent();
		Trial currentTrial = null;
		String previousScene = "";
		int trialNumber = 1;
		for (PupillometryFileLine line : lines) {
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
						logErrorNotifcation("Multiple start events within a single trial were detected.");
						return Collections.emptyList();
					}
					currentTrial = new Trial(trialNumber++);
					trials.add(currentTrial);
				}

				previousScene = scene;
			}

			if (currentTrial != null) {
				currentTrial.addLine(line);
			}
		}

		return trials;
	}
}
