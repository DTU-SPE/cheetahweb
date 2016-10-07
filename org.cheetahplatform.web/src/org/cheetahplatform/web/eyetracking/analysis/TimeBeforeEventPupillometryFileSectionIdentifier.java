package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.common.Assert;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class TimeBeforeEventPupillometryFileSectionIdentifier implements IPupillometryFileBaselineIdentifier {

	private long startTimeStamp;
	private long endTimeStamp;
	private PupillometryFileColumn timeStampColumn;

	public TimeBeforeEventPupillometryFileSectionIdentifier(PupillometryFileLine firstStimulusLine, Integer durationBeforeStimulus,
			PupillometryFileColumn timeStampColumn) {
		Assert.isNotNull(durationBeforeStimulus);
		Assert.isNotNull(firstStimulusLine);
		Assert.isNotNull(timeStampColumn);
		this.timeStampColumn = timeStampColumn;

		// convert to nanos
		durationBeforeStimulus *= 1000;
		endTimeStamp = firstStimulusLine.getLong(timeStampColumn);
		startTimeStamp = endTimeStamp - durationBeforeStimulus;
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
