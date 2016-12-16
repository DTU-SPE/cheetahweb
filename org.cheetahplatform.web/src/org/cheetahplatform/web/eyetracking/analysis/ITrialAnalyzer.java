package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public interface ITrialAnalyzer {
	Map<String, List<ReportableResultEntry>> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) throws Exception;

	String getName();
}
