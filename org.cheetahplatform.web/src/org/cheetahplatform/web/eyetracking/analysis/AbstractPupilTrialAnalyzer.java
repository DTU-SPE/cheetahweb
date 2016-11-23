package org.cheetahplatform.web.eyetracking.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileUtils;

public abstract class AbstractPupilTrialAnalyzer extends AbstractTrialAnalyzer {

	public AbstractPupilTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType analyzeStep) {
		super(config, processing, analyzeStep);
	}

	protected void addAveragePupilSizeToResults(Trial trial, Map<String, String> results, double left, double right) {
		if (left != 0 && right != 0) {
			double average = (left + right) / 2;
			addResult(results, trial, average, PUPIL_AVERAGE);
		}
	}

	@Override
	public Map<String, String> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) throws Exception {
		PupillometryFileColumn leftPupilColumn = pupillometryFile.getHeader().getColumn(dataProcessing.getLeftPupilColumn());
		PupillometryFileColumn rightPupilColumn = pupillometryFile.getHeader().getColumn(dataProcessing.getRightPupilColumn());

		Map<String, String> results = new HashMap<>();

		List<Trial> trials = trialEvaluation.getTrials();
		for (Trial trial : trials) {
			analyzeTrial(trial, results, leftPupilColumn, rightPupilColumn);
		}

		return results;
	}

	protected abstract void analyzeTrial(Trial trial, Map<String, String> results, PupillometryFileColumn leftPupilColumn,
			PupillometryFileColumn rightPupilColumn);

	protected double calculateMean(List<PupillometryFileLine> lines, PupillometryFileColumn column) {
		double[] pupils = PupillometryFileUtils.getPupilValues(lines, column, false);
		return new Mean().evaluate(pupils);
	}

	protected double calculateStandardDeviation(List<PupillometryFileLine> lines, PupillometryFileColumn column) {
		double[] pupils = PupillometryFileUtils.getPupilValues(lines, column, false);
		return new StandardDeviation().evaluate(pupils);
	}

	protected double[] divideByBaseline(List<PupillometryFileLine> lines, PupillometryFileColumn leftPupilColumn, double leftBaseline) {
		double[] leftPupils = PupillometryFileUtils.getPupilValues(lines, leftPupilColumn, false);
		for (int i = 0; i < leftPupils.length; i++) {
			leftPupils[i] = leftPupils[i] / leftBaseline;
		}
		return leftPupils;
	}

	protected double[] subtractBaseline(List<PupillometryFileLine> lines, PupillometryFileColumn column, double baseline) {
		double[] pupils = PupillometryFileUtils.getPupilValues(lines, column, false);
		for (int i = 0; i < pupils.length; i++) {
			pupils[i] = pupils[i] - baseline;
		}
		return pupils;
	}
}