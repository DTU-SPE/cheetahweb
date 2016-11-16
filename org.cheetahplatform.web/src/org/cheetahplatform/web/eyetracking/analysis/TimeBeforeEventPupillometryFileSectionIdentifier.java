package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.common.Assert;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class TimeBeforeEventPupillometryFileSectionIdentifier implements IPupillometryFileBaselineIdentifier {

	public static final String BASELINE_TYPE = "baseline-duration-before-stimulus";
	private long startTimeStamp;
	private long endTimeStamp;
	private PupillometryFileColumn timeStampColumn;
	private int durationBeforeStimulus;

	public TimeBeforeEventPupillometryFileSectionIdentifier(PupillometryFileLine firstStimulusLine, int durationBeforeStimulus,
			PupillometryFileColumn timeStampColumn) {
		Assert.isNotNull(durationBeforeStimulus);
		Assert.isNotNull(firstStimulusLine);
		Assert.isNotNull(timeStampColumn);
		this.timeStampColumn = timeStampColumn;

		// convert to nanos
		durationBeforeStimulus *= 1000;
		this.durationBeforeStimulus = durationBeforeStimulus;
		endTimeStamp = firstStimulusLine.getLong(timeStampColumn);
		startTimeStamp = endTimeStamp - durationBeforeStimulus;
	}

	@Override
	public String getBaselineType() {
		return BASELINE_TYPE;
	}

	@Override
	public List<TrialDetectionNotification> isValidBaseline(Trial trial, Baseline baseline, PupillometryFileColumn timestampColumn) {
		List<TrialDetectionNotification> notifications = new ArrayList<>();
		long duration = baseline.getDuration(timestampColumn);
		double average = duration / baseline.getLines().size();

		if (duration < durationBeforeStimulus - (4 * average)) {
			notifications.add(new TrialDetectionNotification("The duration of the baseline in trial " + trial.getTrialNumber() + " is "
					+ (duration / 1000) + " ms which is considerably shorter than the specified baseline duration of "
					+ (durationBeforeStimulus / 1000) + " ms.", TrialDetectionNotification.TYPE_WARNING));
		} else if (duration < durationBeforeStimulus - (3 * average)) {
			notifications.add(new TrialDetectionNotification(
					"The duration of the baseline in trial " + trial.getTrialNumber() + " is " + (duration / 1000)
							+ " ms which is slightly shorter than the specified baseline duration of " + (durationBeforeStimulus / 1000)
							+ " ms. If this happens often you might want to check the logging of your eyetracker as it indicates unsteady logging.",
					TrialDetectionNotification.TYPE_INFO));
		}
		return notifications;
	}

	@Override
	public boolean isWithinRange(IPupillometryFileLine line) {
		long timestamp = ((PupillometryFileLine) line).getLong(timeStampColumn);
		if (timestamp >= startTimeStamp && timestamp < endTimeStamp) {
			return true;
		}

		return false;
	}
}
