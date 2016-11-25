package org.cheetahplatform.web.eyetracking.analysis.steps;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.cheetahplatform.web.eyetracking.analysis.AbstractPupilTrialAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.AnalyzeConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class AbsoluteTrialAnalyzer extends AbstractPupilTrialAnalyzer {
	private UnivariateStatistic statistic;

	public AbsoluteTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType type,
			UnivariateStatistic statistic, long startTime, long endTime) {
		super(config, processing, type, startTime, endTime);

		this.statistic = statistic;
	}

	@Override
	protected void analyzeTrial(PupillometryFile file, Trial trial, Map<String, String> results, PupillometryFileColumn leftPupilColumn,
			PupillometryFileColumn rightPupilColumn) {
		Stimulus stimulus = trial.getStimulus();
		if (stimulus == null) {
			return;
		}

		List<PupillometryFileLine> lines = stimulus.getLines();
		if (lines.isEmpty()) {
			return;
		}

		double[] leftPupils = getPupilValues(file, leftPupilColumn, lines);
		double leftValue = statistic.evaluate(leftPupils);
		addResult(results, trial, leftValue, PUPIL_LEFT);

		double[] rightPupils = getPupilValues(file, rightPupilColumn, lines);
		double rightValue = statistic.evaluate(rightPupils);
		addResult(results, trial, rightValue, PUPIL_RIGHT);

		addAveragePupilSizeToResults(trial, results, leftValue, rightValue);
	}

}
