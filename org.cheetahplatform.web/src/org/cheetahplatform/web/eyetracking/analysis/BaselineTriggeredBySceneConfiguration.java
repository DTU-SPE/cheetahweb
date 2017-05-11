package org.cheetahplatform.web.eyetracking.analysis;

public class BaselineTriggeredBySceneConfiguration extends BaselineConfiguration {
	private String baselineStart;
	private String baselineEnd;
	private long startOffset;
	private long endOffset;

	public String getBaselineEnd() {
		return baselineEnd;
	}

	public String getBaselineStart() {
		return baselineStart;
	}

	public long getEndOffset() {
		return endOffset;
	}

	public long getStartOffset() {
		return startOffset;
	}

	public void setBaselineEnd(String stimulusEnd) {
		this.baselineEnd = stimulusEnd;
	}

	public void setBaselineStart(String stimulusStart) {
		this.baselineStart = stimulusStart;
	}

	public void setEndOffset(long delayedEnd) {
		this.endOffset = delayedEnd;
	}

	public void setStartOffset(long delayedStart) {
		this.startOffset = delayedStart;
	}
}
