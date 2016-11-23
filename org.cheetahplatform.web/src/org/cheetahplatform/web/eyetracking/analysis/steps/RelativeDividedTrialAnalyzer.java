package org.cheetahplatform.web.eyetracking.analysis.steps;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.cheetahplatform.web.eyetracking.analysis.AbstractPupilTrialAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.AnalyzeConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.Baseline;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public  class RelativeDividedTrialAnalyzer extends AbstractPupilTrialAnalyzer {

	private UnivariateStatistic statistic;

	public RelativeDividedTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType type,
			UnivariateStatistic statistic) {
		super(config, processing, type);

		this.statistic = statistic;
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

		double leftValue = 0;
		if (leftBaseline > 0) {
			double[] leftPupils = divideByBaseline(stimulus.getLines(), leftPupilColumn, leftBaseline);
			leftValue = statistic.evaluate(leftPupils);
			addResult(results, trial, leftValue, PUPIL_LEFT);
		}

		double rightValue = 0;
		if (rightBaseline > 0) {
			double[] rightPupils = divideByBaseline(stimulus.getLines(), rightPupilColumn, rightBaseline);
			rightValue = statistic.evaluate(rightPupils);
			addResult(results, trial, rightValue, PUPIL_RIGHT);
		}

		addAveragePupilSizeToResults(trial, results, leftValue, rightValue);
	}
}
