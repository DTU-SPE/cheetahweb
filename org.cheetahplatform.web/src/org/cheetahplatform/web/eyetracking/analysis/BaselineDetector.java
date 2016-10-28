package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class BaselineDetector extends AbstractPupillopmetryFileDetector {

	private Trial trial;
	private TrialConfiguration config;
	private IPupillometryFileBaselineIdentifier baselineIdentifier;
	private PupillometryFileColumn timestampColumn;

	public BaselineDetector(Trial trial, TrialConfiguration config, PupillometryFileColumn timestampColumn) {
		this.trial = trial;
		this.config = config;
		this.timestampColumn = timestampColumn;
		readConfig();
	}

	public void detectBaseline() {
		if (baselineIdentifier == null) {
			return;
		}

		Baseline baseline = null;
		for (PupillometryFileLine line : trial.getLines()) {
			if (baselineIdentifier.isWithinRange(line)) {
				if (baseline == null) {
					baseline = new Baseline();
				}

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
		if (calculationMethod.equals("baseline-duration-before-stimulus")) {
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
