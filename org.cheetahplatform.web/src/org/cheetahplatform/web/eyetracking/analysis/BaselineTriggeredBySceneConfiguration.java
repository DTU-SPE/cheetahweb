package org.cheetahplatform.web.eyetracking.analysis;

public class BaselineTriggeredBySceneConfiguration extends BaselineConfiguration {
	private String baselineStart;
	private String baselineEnd;

	public String getBaselineEnd() {
		return baselineEnd;
	}

	public String getBaselineStart() {
		return baselineStart;
	}

	public void setBaselineEnd(String stimulusEnd) {
		this.baselineEnd = stimulusEnd;
	}

	public void setBaselineStart(String stimulusStart) {
		this.baselineStart = stimulusStart;
	}
}
