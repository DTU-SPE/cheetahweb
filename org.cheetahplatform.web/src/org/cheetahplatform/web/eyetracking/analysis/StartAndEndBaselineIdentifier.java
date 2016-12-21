package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class StartAndEndBaselineIdentifier extends StartAndEndPupillometryFileSectionIdentifier
		implements IPupillometryFileBaselineIdentifier {
	public static final String BASELINE_TYPE = "baseline-scene-trigger";
	private PupillometryFileColumn studioEventDataColumn;

	public StartAndEndBaselineIdentifier(String start, String end, PupillometryFileColumn studioEventDataColumn) {
		super(start, end);
		this.studioEventDataColumn = studioEventDataColumn;
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
			return true;
		}

		if (isStart(line, studioEventDataColumn)) {
			return false;
		}

		return started;
	}

}
