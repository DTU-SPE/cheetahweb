package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Map;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public interface ITrialAnalyzer {
	Map<String, String> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) throws Exception;

	String getName();
}
