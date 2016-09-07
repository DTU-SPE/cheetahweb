package org.cheetahplatform.web.dto;

import java.util.HashMap;
import java.util.Map;

public class MetricsDto {
	private String modelId;
	private String workflowId;
	private String experiment;
	private Map<String, String> metrics;

	public MetricsDto(String modelId, String workflowId, String experiment) {
		this.modelId = modelId;
		this.workflowId = workflowId;
		this.experiment = experiment;
		this.metrics = new HashMap<>();
	}

	public void addMetric(String name, String value) {
		metrics.put(name, value);
	}

	public String getExperiment() {
		return experiment;
	}

	public Map<String, String> getMetrics() {
		return metrics;
	}

	public String getModelId() {
		return modelId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

}
