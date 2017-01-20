package org.cheetahplatform.web.eyetracking.analysis.steps;

import static org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType.BLINKS;
import static org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType.MISSING_PERCENT;
import static org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType.MISSING_TOTAL;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingStep;
import org.cheetahplatform.web.eyetracking.analysis.ITrialAnalyzer;
import org.eclipse.core.runtime.Assert;

public class TrialAnalyzerFactory {
	public static ITrialAnalyzer createAnalyzer(AnalyzeConfiguration config, DataProcessing processing,
			DataProcessingStep dataProcessingStep) {
		Assert.isNotNull(config);
		String typeId = config.getType();
		AnalyzeStepType type = AnalyzeStepType.byId(typeId);
		if (type.equals(BLINKS)) {
			return new BlinkTrialAnalyzer(config, processing, dataProcessingStep);
		}
		if (type.equals(MISSING_TOTAL) || type.equals(MISSING_PERCENT)) {
			return new MissingTrialAnalyzer(config, processing, type, dataProcessingStep);
		}

		UnivariateStatistic statistic = null;
		if (typeId.startsWith("mean")) {
			statistic = new Mean();
		} else if (typeId.startsWith("standard_deviation")) {
			statistic = new StandardDeviation();
		} else if (typeId.startsWith("standard_error")) {
			statistic = new StandardError();
		} else if (typeId.startsWith("median")) {
			statistic = new Median();
		} else if (typeId.startsWith("maximum")) {
			statistic = new Max();
		} else if (typeId.startsWith("minimum")) {
			statistic = new Min();
		} else {
			throw new IllegalArgumentException("Unknown trial analyzer type: " + typeId);
		}

		long startTime = config.getStartTime();
		long endTime = config.getEndTime();
		if (typeId.endsWith("absolute")) {
			return new AbsoluteTrialAnalyzer(config, processing, type, statistic, startTime, endTime, dataProcessingStep);
		}
		if (typeId.endsWith("relative_divided")) {
			return new RelativeDividedTrialAnalyzer(config, processing, type, statistic, startTime, endTime, dataProcessingStep);
		}
		if (typeId.endsWith("relative_subtracted")) {
			return new RelativeSubtractedTrialAnalyzer(config, processing, type, statistic, startTime, endTime, dataProcessingStep);
		}
		throw new IllegalArgumentException("Unknown trial analyzer type: " + typeId);
	}
}
