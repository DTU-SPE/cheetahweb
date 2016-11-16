package org.cheetahplatform.web.eyetracking.analysis;

public class TrialConfiguration {
	private String trialStart;
	private String trialEnd;
	private boolean useTrialStartForTrialEnd;
	private BaselineConfiguration baseline;
	private StimulusConfiguration stimulus;
	private int ignoredTrials;

	public BaselineConfiguration getBaseline() {
		return baseline;
	}

	public int getIgnoredTrials() {
		return ignoredTrials;
	}

	public StimulusConfiguration getStimulus() {
		return stimulus;
	}

	public String getTrialEnd() {
		return trialEnd;
	}

	public String getTrialStart() {
		return trialStart;
	}

	public boolean isUseTrialStartForTrialEnd() {
		return useTrialStartForTrialEnd;
	}

	public void setBaseline(BaselineConfiguration baseline) {
		this.baseline = baseline;
	}

	public void setIgnoredTrials(int ignoredTrials) {
		this.ignoredTrials = ignoredTrials;
	}

	public void setStimulus(StimulusConfiguration stimulus) {
		this.stimulus = stimulus;
	}

	public void setTrialEnd(String trialEnd) {
		this.trialEnd = trialEnd;
	}

	public void setTrialStart(String trialStart) {
		this.trialStart = trialStart;
	}

	public void setUseTrialStartForTrialEnd(boolean useTrialStartForTrialEnd) {
		this.useTrialStartForTrialEnd = useTrialStartForTrialEnd;
	}
}
