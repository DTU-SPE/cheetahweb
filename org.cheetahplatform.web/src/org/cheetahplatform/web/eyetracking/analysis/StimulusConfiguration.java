package org.cheetahplatform.web.eyetracking.analysis;

public class StimulusConfiguration {
	private String stimulusStart;
	private String stimulusEnd;
	private boolean stimulusEndsWithTrialEnd;

	public String getStimulusEnd() {
		return stimulusEnd;
	}

	public String getStimulusStart() {
		return stimulusStart;
	}

	public boolean isStimulusEndsWithTrialEnd() {
		return stimulusEndsWithTrialEnd;
	}

	public void setStimulusEnd(String stimulusEnd) {
		this.stimulusEnd = stimulusEnd;
	}

	public void setStimulusEndsWithTrialEnd(boolean stimulusEndsWithTrialEnd) {
		this.stimulusEndsWithTrialEnd = stimulusEndsWithTrialEnd;
	}

	public void setStimulusStart(String stimulusStart) {
		this.stimulusStart = stimulusStart;
	}
}
