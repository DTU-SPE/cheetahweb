package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public interface IPupillometryFileBaselineIdentifier {

	List<TrialDetectionNotification> isValidBaseline(Trial trial, Baseline baseline, PupillometryFileColumn timestampColumn);

	boolean isWithinRange(IPupillometryFileLine line);

}
