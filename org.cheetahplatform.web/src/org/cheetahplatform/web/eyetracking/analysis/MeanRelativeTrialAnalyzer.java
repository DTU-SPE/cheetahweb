package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Map;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public class MeanRelativeTrialAnalyzer extends AbstractTrialAnalyzer {

	public MeanRelativeTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing) {
		super(config, processing, AnalyzeStepType.MEAN_RELATIVE.getName());
	}

	@Override
	public Map<String, String> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) {
		throw new UnsupportedOperationException("Implement me!");
	}
}
