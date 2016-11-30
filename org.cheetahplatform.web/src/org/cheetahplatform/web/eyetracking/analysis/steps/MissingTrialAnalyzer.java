package org.cheetahplatform.web.eyetracking.analysis.steps;

import java.util.Map;

import org.cheetahplatform.web.eyetracking.analysis.AbstractTrialAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public class MissingTrialAnalyzer extends AbstractTrialAnalyzer {

	public MissingTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing) {
		super(config, processing, AnalyzeStepType.MISSING_TOTAL);
	}

	@Override
	public Map<String, String> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
