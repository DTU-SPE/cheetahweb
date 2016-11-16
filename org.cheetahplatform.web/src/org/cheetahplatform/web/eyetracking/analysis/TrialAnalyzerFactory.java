package org.cheetahplatform.web.eyetracking.analysis;

import org.eclipse.core.runtime.Assert;

public class TrialAnalyzerFactory {
	public static ITrialAnalyzer createAnalyzer(AnalyzeConfiguration config, DataProcessing processing) {
		Assert.isNotNull(config);
		String type = config.getType();
		if (AnalyzeStepType.MEAN_ABSOLUTE.getId().equals(type)) {
			return new MeanAbsoluteTrialAnalyzer(config, processing);
		}

		if (AnalyzeStepType.MEAN_RELATIVE.getId().equals(type)) {
			return new MeanRelativeTrialAnalyzer(config, processing);
		}

		throw new IllegalArgumentException("Unknown trial analyzer type: " + type);
	}
}
