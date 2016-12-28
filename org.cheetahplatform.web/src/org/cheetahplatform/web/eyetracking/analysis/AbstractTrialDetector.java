package org.cheetahplatform.web.eyetracking.analysis;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public abstract class AbstractTrialDetector extends AbstractPupillopmetryFileDetector {
	private static final String TRIAL_NUMBER_COLUMN = "Trial_number";
	private static final String TIME_SINCE_TRIAL_START = "Time_since_trial_start";

	protected long fileId;
	protected TrialConfiguration config;
	protected String decimalSeparator;
	private IPupillometryFileSectionIdentifier trialIdentifier;
	protected String timestampColumn;
	private DataProcessing dataProcessing;

	public AbstractTrialDetector(long fileId, DataProcessing dataProcessing, TrialConfiguration config, String decimalSeparator,
			String timestampColumn) {
		this.fileId = fileId;
		this.dataProcessing = dataProcessing;
		this.config = config;
		this.decimalSeparator = decimalSeparator;
		this.timestampColumn = timestampColumn;

		readConfig();
	}

	private void addRelativeValues(List<Trial> trials, PupillometryFileColumn source, PupillometryFileColumn target, boolean divide)
			throws IOException {
		for (Trial trial : trials) {
			double baseline = trial.getBaseline().calculateMean(source);
			if (baseline > 0.0) {
				Stimulus stimulus = trial.getStimulus();
				if (stimulus == null) {
					continue;
				}

				for (PupillometryFileLine line : stimulus.getLines()) {
					Double value = line.getDouble(source);
					if (value != null) {
						double relativeValue = 0;
						if (divide) {
							relativeValue = value / baseline;
						} else {
							relativeValue = value - baseline;
						}
						line.setValue(target, relativeValue);
					}
				}
			}
		}
	}

	/**
	 * Adds the relative pupil values during the stimulus, #658.
	 *
	 * @param pupillometryFile
	 * @throws IOException
	 */
	private void addRelativeValues(PupillometryFile pupillometryFile, List<Trial> trials) throws IOException {
		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn baselineColumn = header.getColumn(BaselineDetector.BASELINE_COLUMN_NAME);
		PupillometryFileColumn leftPupilColumn = header.getColumn(dataProcessing.getLeftPupilColumn());
		PupillometryFileColumn rightPupilColumn = header.getColumn(dataProcessing.getRightPupilColumn());
		if (baselineColumn == null || leftPupilColumn == null || rightPupilColumn == null) {
			return;
		}

		PupillometryFileColumn leftRelativeDivided = pupillometryFile.appendColumn("pupil_left_relative_divided");
		PupillometryFileColumn rightRelativeDivided = pupillometryFile.appendColumn("pupil_right_relative_divided");
		addRelativeValues(trials, leftPupilColumn, leftRelativeDivided, true);
		addRelativeValues(trials, rightPupilColumn, rightRelativeDivided, true);

		PupillometryFileColumn leftRelativeSubtracted = pupillometryFile.appendColumn("pupil_left_relative_subtracted");
		PupillometryFileColumn rightRelativeSubtracted = pupillometryFile.appendColumn("pupil_right_relative_subtracted");
		addRelativeValues(trials, leftPupilColumn, leftRelativeSubtracted, false);
		addRelativeValues(trials, rightPupilColumn, rightRelativeSubtracted, false);
	}

	private void detectBaseline(PupillometryFileColumn timeStamp, List<Trial> trials, PupillometryFile pupillometryFile) throws Exception {
		for (Trial trial : trials) {
			BaselineDetector baselineDetector = new BaselineDetector(trial, config, timeStamp, pupillometryFile);
			baselineDetector.detectBaseline();
		}
	}

	private void detectStimulus(PupillometryFile pupillometryFile, List<Trial> trials) throws Exception {
		PupillometryFileColumn timestamp = pupillometryFile.getHeader().getColumn(timestampColumn);

		for (Trial trial : trials) {
			StimulusDetector stimulusDetector = new StimulusDetector(trial, config, pupillometryFile, timestamp);
			Stimulus stimulus = stimulusDetector.detectStimulus();
			trial.setStimulus(stimulus);
			trial.addAllNotifications(stimulusDetector.getNotifications());
		}
	}

	public TrialEvaluation detectTrials() throws Exception {
		return detectTrials(true, true, true);
	}

	public TrialEvaluation detectTrials(boolean detectStimulus, boolean detectBaseline, boolean addRelativeValues) throws Exception {
		PupillometryFile pupillometryFile = loadPupillometryFile();

		List<Trial> trials = splitFileIntoTrials(pupillometryFile);
		if (trials.isEmpty()) {
			return new TrialEvaluation(getNotifications());
		}

		if (detectStimulus) {
			detectStimulus(pupillometryFile, trials);
		}
		if (detectBaseline) {
			detectBaseline(pupillometryFile.getHeader().getColumn(timestampColumn), trials, pupillometryFile);
		}
		// add relative pupil values duration the stimulus, #658
		if (addRelativeValues) {
			addRelativeValues(pupillometryFile, trials);
		}

		return new TrialEvaluation(trials, getNotifications());
	}

	protected abstract PupillometryFile loadPupillometryFile() throws SQLException, FileNotFoundException, IOException;

	private void readConfig() {
		String trialStart = config.getTrialStart();
		String trialEnd = null;
		if (config.isUseTrialStartForTrialEnd()) {
			trialIdentifier = new StartPupillometryFileSectionIdentifier(trialStart);
		} else {
			trialEnd = config.getTrialEnd();
			trialIdentifier = new StartAndEndPupillometryFileSectionIdentifier(trialStart, trialEnd);
		}
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
		PupillometryFileColumn trialNumberColumn = initializeColumn(pupillometryFile, TRIAL_NUMBER_COLUMN);
		PupillometryFileColumn relativeTimeColumn = initializeColumn(pupillometryFile, TIME_SINCE_TRIAL_START);

		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn studioEventDataColumn = header.getColumn(STUDIO_EVENT_DATA);
		PupillometryFileColumn timeStampColumn = header.getColumn(timestampColumn);

		List<PupillometryFileLine> lines = pupillometryFile.getContent();
		Trial currentTrial = null;
		int trialNumber = 1;
		int trialsToIgnore = config.getIgnoredTrials();

		for (PupillometryFileLine line : lines) {
			// the marking with studio event is always added to the previous line --> for start event, add the lines to the next trial, for
			// end event, add the lines to the next trial
			if (currentTrial != null) {
				line.setValue(trialNumberColumn, currentTrial.getTrialNumber());
				addRelativeTime(relativeTimeColumn, timeStampColumn, currentTrial.getLines(), line);
				currentTrial.addLine(line);
			}

			if (trialIdentifier.isEnd(line, studioEventDataColumn)) {
				currentTrial = null;
			}

			if (trialIdentifier.isStart(line, studioEventDataColumn)) {
				if (trialsToIgnore > 0) {
					trialsToIgnore--;
				} else {
					currentTrial = new Trial(trialNumber++);
					trials.add(currentTrial);
				}
			}
		}

		return trials;
	}
}