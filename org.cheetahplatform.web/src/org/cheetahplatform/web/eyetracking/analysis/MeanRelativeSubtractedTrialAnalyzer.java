package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class MeanRelativeSubtractedTrialAnalyzer extends AbstractPupilTrialAnalyzer {

	public MeanRelativeSubtractedTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing) {
		super(config, processing, AnalyzeStepType.MEAN_RELATIVE_SUBTRACTED);
	}

	@Override
	protected void analyzeTrial(Trial trial, Map<String, String> results, PupillometryFileColumn leftPupilColumn,
			PupillometryFileColumn rightPupilColumn) {
		Baseline baseline = trial.getBaseline();
		if (baseline == null || baseline.getLines().isEmpty()) {
			return;
		}

		Stimulus stimulus = trial.getStimulus();
		if (stimulus == null || stimulus.getLines().isEmpty()) {
			return;
		}

		double leftBaseline = calculateMean(baseline.getLines(), leftPupilColumn);
		double rightBaseline = calculateMean(baseline.getLines(), rightPupilColumn);

		double leftMean = 0;
		if (leftBaseline > 0) {
			double[] leftPupils = subtractBaseline(stimulus.getLines(), leftPupilColumn, leftBaseline);
			leftMean = new Mean().evaluate(leftPupils);
			addResult(results, trial, leftMean, PUPIL_LEFT);
		}

		double rightMean = 0;
		if (rightBaseline > 0) {
			double[] rightPupils = subtractBaseline(stimulus.getLines(), rightPupilColumn, rightBaseline);
			rightMean = new Mean().evaluate(rightPupils);
			addResult(results, trial, rightMean, PUPIL_RIGHT);
		}

		addAveragePupilSizeToResults(trial, results, leftMean, rightMean);
	}
}
