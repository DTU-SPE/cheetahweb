package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class StartAndEndBaselineIdentifier extends StartAndEndPupillometryFileSectionIdentifier
		implements IPupillometryFileBaselineIdentifier {
	public static final String BASELINE_TYPE = "baseline-scene-trigger";
	private PupillometryFileColumn studioEventDataColumn;
	private BaselineTriggeredBySceneConfiguration configuration;
	private PupillometryFileColumn timeStampColumn;
	private long startTimestamp;

	public StartAndEndBaselineIdentifier(BaselineTriggeredBySceneConfiguration castedConfiguration,
			PupillometryFileColumn studioEventDataColumn, PupillometryFileColumn timeStampColumn) {
		super(castedConfiguration.getBaselineStart(), castedConfiguration.getBaselineEnd());
		this.configuration = castedConfiguration;
		this.studioEventDataColumn = studioEventDataColumn;
		this.timeStampColumn = timeStampColumn;
	}

	public boolean checkOffset(IPupillometryFileLine line) {
		long startOffset = configuration.getStartOffset() * 1000;
		long endOffset = configuration.getEndOffset() * 1000;

		// no adaptations - just return started
		if (startOffset <= 0 && endOffset <= 0) {
			return true;
		}

		// start and end specified
		long currentTimeStamp = Long.parseLong(line.get(timeStampColumn));
		if (startOffset > 0 && endOffset > 0) {
			long start = startTimestamp + startOffset;
			long end = startTimestamp + endOffset;

			return start <= currentTimeStamp && currentTimeStamp <= end;
		}

		if (startOffset > 0) {
			long start = startTimestamp + startOffset;
			return start <= currentTimeStamp;
		}

		if (endOffset > 0) {
			long end = startTimestamp + endOffset;
			return currentTimeStamp <= end;
		}

		return false;
	}

	@Override
	public String getBaselineType() {
		return BASELINE_TYPE;
	}

	@Override
	public List<TrialDetectionNotification> isValidBaseline(Trial trial, Baseline baseline, PupillometryFileColumn timestampColumn) {
		return Collections.emptyList();
	}

	@Override
	public boolean isWithinRange(IPupillometryFileLine line) {
		if (isEnd(line, studioEventDataColumn)) {
			return checkOffset(line);
		}

		if (isStart(line, studioEventDataColumn)) {
			startTimestamp = Long.parseLong(line.get(timeStampColumn));
			return false;
		}

		if (started) {
			return checkOffset(line);
		}

		return false;
	}
}
