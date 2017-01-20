package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.dto.TrialAnalysisReportableResultEntry;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeConfiguration;
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
	private DataProcessingStep dataProcessingStep;

	public AbstractTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType analyzeStep,
			DataProcessingStep dataProcessingStep) {
		Assert.isNotNull(dataProcessingStep);
		Assert.isNotNull(config);
		Assert.isNotNull(analyzeStep);
		Assert.isLegal(!analyzeStep.getName().trim().isEmpty());
		Assert.isNotNull(processing);
		this.config = config;
		this.analyzeStep = analyzeStep;
		this.dataProcessingStep = dataProcessingStep;
		this.dataProcessing = processing;
		this.name = analyzeStep.getName();
	}

	public void addResult(Map<String, List<ReportableResultEntry>> results, String key, TrialAnalysisReportableResultEntry entry) {
		if (!results.containsKey(key)) {
			results.put(key, new ArrayList<ReportableResultEntry>());
		}
		results.get(key).add(entry);
	}

	protected void addResult(Map<String, List<ReportableResultEntry>> results, Trial trial, double value, String operation) {
		String key = "";
		if (dataProcessingStep.getName() != null) {
			key = dataProcessingStep.getName();
		}

		key = key + RESULT_SEPARATOR + analyzeStep.getId() + RESULT_SEPARATOR + operation;
		TrialAnalysisReportableResultEntry entry = new TrialAnalysisReportableResultEntry(trial.getTrialNumber(), String.valueOf(value));
		addResult(results, key, entry);
	}

	@Override
	public String getName() {
		return name;
	}
}