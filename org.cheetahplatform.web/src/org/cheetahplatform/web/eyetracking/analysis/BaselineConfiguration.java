package org.cheetahplatform.web.eyetracking.analysis;

public class BaselineConfiguration {
	private boolean noBaseline;
	private String baselineCalculation;
	private Integer durationBeforeStimulus;

	public String getBaselineCalculation() {
		return baselineCalculation;
	}

	public Integer getDurationBeforeStimulus() {
		return durationBeforeStimulus;
	}

	public boolean isNoBaseline() {
		return noBaseline;
	}

	public void setBaselineCalculation(String baselineCalculation) {
		this.baselineCalculation = baselineCalculation;
	}

	public void setDurationBeforeStimulus(Integer durationBeforeStimulus) {
		this.durationBeforeStimulus = durationBeforeStimulus;
	}

	public void setNoBaseline(boolean noBaseline) {
		this.noBaseline = noBaseline;
	}
}
