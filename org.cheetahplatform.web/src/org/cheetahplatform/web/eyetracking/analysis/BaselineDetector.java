package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class BaselineDetector extends AbstractPupillopmetryFileDetector {

	private static final String BASELINE_COLUMN_NAME = "Baseline";
	private static final String TIME_SINCE_BASELINE_START = "Time_since_baseline_start";

	private Trial trial;
	private TrialConfiguration config;
	private IPupillometryFileBaselineIdentifier baselineIdentifier;
	private PupillometryFileColumn timestampColumn;
	private PupillometryFile pupillometryFile;

	public BaselineDetector(Trial trial, TrialConfiguration config, PupillometryFileColumn timestampColumn,
			PupillometryFile pupillometryFile) {
		this.trial = trial;
		this.config = config;
		this.timestampColumn = timestampColumn;
		this.pupillometryFile = pupillometryFile;
		readConfig();
	}

	public void detectBaseline() throws Exception {
		if (baselineIdentifier == null) {
			return;
		}

		PupillometryFileColumn baseLineColumn = initializeColumn(pupillometryFile, BASELINE_COLUMN_NAME);
		PupillometryFileColumn relativeTimeColumn = initializeColumn(pupillometryFile, TIME_SINCE_BASELINE_START);

		Baseline baseline = null;
		for (PupillometryFileLine line : trial.getLines()) {
			if (baselineIdentifier.isWithinRange(line)) {
				if (baseline == null) {
					baseline = new Baseline(baselineIdentifier.getBaselineType());
				}

				line.setValue(baseLineColumn, Boolean.TRUE.toString());
				addRelativeTime(relativeTimeColumn, timestampColumn, baseline.getLines(), line);
				baseline.addLine(line);
			}
		}

		if (baseline != null) {
			trial.setBaseline(baseline);
		}

		if (baseline == null) {
			logWarningNotifcation("Could not identify a baseline for trial " + trial.getTrialNumber() + ".");
		} else {
			trial.addAllNotifications(baselineIdentifier.isValidBaseline(trial, baseline, timestampColumn));
		}

		trial.addAllNotifications(getNotifications());
	}

	private void readConfig() {
		BaselineConfiguration baselineConfiguration = config.getBaseline();
		String calculationMethod = baselineConfiguration.getBaselineCalculation();
		if (calculationMethod.equals(TimeBeforeEventPupillometryFileSectionIdentifier.BASELINE_TYPE)) {
			if (!trial.hasStimulus()) {
				return;
			}

			IPupillometryFileLine firstStimulusLine = trial.getStimulus().getLines().get(0);
			Integer durationBeforeStimulus = baselineConfiguration.getDurationBeforeStimulus();
			baselineIdentifier = new TimeBeforeEventPupillometryFileSectionIdentifier((PupillometryFileLine) firstStimulusLine,
					durationBeforeStimulus, timestampColumn);
		}
	}
}
