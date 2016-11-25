package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Map;

import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType;
import org.eclipse.core.runtime.Assert;

public abstract class AbstractTrialAnalyzer implements ITrialAnalyzer {
	protected static final String RESULT_SEPARATOR = "_";
	protected static final String PUPIL_LEFT = "pupil_left";
	protected static final String PUPIL_RIGHT = "pupil_right";
	protected static final String PUPIL_AVERAGE = "pupil_average";

	protected AnalyzeConfiguration config;
	private String name;
	protected DataProcessing dataProcessing;
	protected AnalyzeStepType analyzeStep;

	public AbstractTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType analyzeStep) {
		this.analyzeStep = analyzeStep;
		Assert.isNotNull(config);
		Assert.isNotNull(analyzeStep);
		Assert.isLegal(!analyzeStep.getName().trim().isEmpty());
		Assert.isNotNull(processing);
		this.config = config;
		this.dataProcessing = processing;
		this.name = analyzeStep.getName();
	}

	protected void addResult(Map<String, String> results, Trial trial, double value, String operation) {
		String key = trial.getIdentifier() + RESULT_SEPARATOR + analyzeStep.getId() + RESULT_SEPARATOR + operation;
		results.put(key, String.valueOf(value));
	}

	@Override
	public String getName() {
		return name;
	}
}