package org.cheetahplatform.web.eyetracking.analysis.steps;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.cheetahplatform.web.eyetracking.analysis.AbstractPupilTrialAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.AnalyzeConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileUtils;

public class AbsoluteTrialAnalyzer extends AbstractPupilTrialAnalyzer {
	private UnivariateStatistic statistic;

	public AbsoluteTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType type,
			UnivariateStatistic statistic) {
		super(config, processing, type);

		this.statistic = statistic;
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

		double[] leftPupils = PupillometryFileUtils.getPupilValues(lines, leftPupilColumn, false);
		double leftValue = statistic.evaluate(leftPupils);
		addResult(results, trial, leftValue, PUPIL_LEFT);

		double[] rightPupils = PupillometryFileUtils.getPupilValues(lines, rightPupilColumn, false);
		double rightValue = statistic.evaluate(rightPupils);
		addResult(results, trial, rightValue, PUPIL_RIGHT);

		addAveragePupilSizeToResults(trial, results, leftValue, rightValue);
	}
}
