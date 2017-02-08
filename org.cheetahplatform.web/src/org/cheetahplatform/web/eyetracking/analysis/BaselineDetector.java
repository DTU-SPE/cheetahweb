package org.cheetahplatform.web.eyetracking.analysis;

import java.io.IOException;
import java.util.ListIterator;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class BaselineDetector extends AbstractPupillopmetryFileDetector {

	public static final String BASELINE_COLUMN_NAME = "Baseline";
	private static final String TIME_SINCE_BASELINE_START = "Time_since_baseline_start";

	private Trial trial;
	private TrialConfiguration config;
	private IPupillometryFileBaselineIdentifier baselineIdentifier;
	private PupillometryFileColumn timestampColumn;
	private PupillometryFile pupillometryFile;

	public BaselineDetector(Trial trial, TrialConfiguration config, PupillometryFileColumn timestampColumn,
			PupillometryFile pupillometryFile) throws IOException {
		this.trial = trial;
		this.config = config;
		this.timestampColumn = timestampColumn;
		this.pupillometryFile = pupillometryFile;
		readConfig();
	}

	public void detectBaseline() throws Exception {
		if (baselineIdentifier == null || config.getBaseline().isNoBaseline()) {
			return;
		}

		PupillometryFileColumn baseLineColumn = initializeColumn(pupillometryFile, BASELINE_COLUMN_NAME);
		PupillometryFileColumn relativeTimeColumn = initializeColumn(pupillometryFile, TIME_SINCE_BASELINE_START);

		// handle the case that trial and stimulus are the same: since the markings are always applied to the previous line, the stimulus
		// cannot be detected, since it lies outside the trial, #651
		if (!trial.getLines().isEmpty()) {
			ListIterator<PupillometryFileLine> iterator = pupillometryFile
					.getIteratorStartingAt(trial.getLines().get(0).getLong(timestampColumn), timestampColumn);
			// previous() needs to called twice to get the previous element: the first invocation will return the same element (see javadoc
			// of ListIterator)
			iterator.previous();
			if (iterator.hasPrevious()) {
				PupillometryFileLine previousLine = iterator.previous();
				baselineIdentifier.isWithinRange(previousLine);
			}
		}

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

	private void readConfig() throws IOException {
		BaselineConfiguration baselineConfiguration = config.getBaseline();
		if (baselineConfiguration instanceof DurationBeforeStimulusBaselineConfiguration) {
			DurationBeforeStimulusBaselineConfiguration castedConfiguration = (DurationBeforeStimulusBaselineConfiguration) baselineConfiguration;

			if (!trial.hasStimulus()) {
				return;
			}

			IPupillometryFileLine firstStimulusLine = trial.getStimulus().getLines().get(0);
			Integer durationBeforeStimulus = castedConfiguration.getDurationBeforeStimulus();
			baselineIdentifier = new TimeBeforeEventPupillometryFileSectionIdentifier((PupillometryFileLine) firstStimulusLine,
					durationBeforeStimulus, timestampColumn);
		} else if (baselineConfiguration instanceof BaselineTriggeredBySceneConfiguration) {
			BaselineTriggeredBySceneConfiguration castedConfiguration = (BaselineTriggeredBySceneConfiguration) baselineConfiguration;
			PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_STUDIO_EVENT_DATA);

			baselineIdentifier = new StartAndEndBaselineIdentifier(castedConfiguration.getBaselineStart(),
					castedConfiguration.getBaselineEnd(), studioEventDataColumn);
		}

	}
}
