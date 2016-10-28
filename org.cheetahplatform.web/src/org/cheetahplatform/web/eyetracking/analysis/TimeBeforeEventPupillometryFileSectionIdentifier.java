package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.common.Assert;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class TimeBeforeEventPupillometryFileSectionIdentifier implements IPupillometryFileBaselineIdentifier {

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

	private long getDuration(Baseline baseline, PupillometryFileColumn timestampColumn) {
		List<PupillometryFileLine> lines2 = baseline.getLines();
		if (lines2 == null || lines2.isEmpty()) {
			return 0;
		}

		PupillometryFileLine firstLine = lines2.get(0);
		PupillometryFileLine lastLine = lines2.get(lines2.size() - 1);

		long startTimeStamp = firstLine.getLong(timestampColumn);
		long endTimeStamp = lastLine.getLong(timestampColumn);
		return endTimeStamp - startTimeStamp;
	}

	@Override
	public List<TrialDetectionNotification> isValidBaseline(Trial trial, Baseline baseline, PupillometryFileColumn timestampColumn) {
		List<TrialDetectionNotification> notifications = new ArrayList<>();
		long duration = getDuration(baseline, timestampColumn);
		if (duration < durationBeforeStimulus) {
			notifications.add(new TrialDetectionNotification(
					"The baseline in trial " + trial.getTrialNumber()
							+ " is shorter than the specified duration since the baseline would need to start prior to the start of the trial.",
					TrialDetectionNotification.TYPE_WARNING));
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
