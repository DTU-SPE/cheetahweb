package org.cheetahplatform.web.eyetracking.analysis;

public class BaselineConfiguration {
	private String baselineCalculation;
	private Integer durationBeforeStimulus;

	public String getBaselineCalculation() {
		return baselineCalculation;
	}

	public Integer getDurationBeforeStimulus() {
		return durationBeforeStimulus;
	}

	public void setBaselineCalculation(String baselineCalculation) {
		this.baselineCalculation = baselineCalculation;
	}

	public void setDurationBeforeStimulus(Integer durationBeforeStimulus) {
		this.durationBeforeStimulus = durationBeforeStimulus;
	}
}
