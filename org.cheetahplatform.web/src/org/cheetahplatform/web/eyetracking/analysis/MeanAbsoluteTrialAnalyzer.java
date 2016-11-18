package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class MeanAbsoluteTrialAnalyzer extends AbstractPupilTrialAnalyzer {
	public MeanAbsoluteTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing) {
		super(config, processing, AnalyzeStepType.MEAN_ABSOLUTE);
	}

	@Override
	protected void analyzeTrial(Trial trial, Map<String, String> results, PupillometryFileColumn leftPupilColumn,
			PupillometryFileColumn rightPupilColumn) {
		Stimulus stimulus = trial.getStimulus();
		if (stimulus == null) {
			return;
		}

		List<PupillometryFileLine> lines = stimulus.getLines();
		if (lines.isEmpty()) {
			return;
		}

		double leftMean = calculateMean(lines, leftPupilColumn);
		addResult(results, trial, leftMean, PUPIL_LEFT);

		double rightMean = calculateMean(lines, rightPupilColumn);
		addResult(results, trial, rightMean, PUPIL_RIGHT);

		addAveragePupilSizeToResults(trial, results, leftMean, rightMean);
	}
}
