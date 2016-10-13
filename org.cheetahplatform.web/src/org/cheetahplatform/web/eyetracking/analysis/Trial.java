package org.cheetahplatform.web.eyetracking.analysis;

public class Trial extends AbstractPupillometryFileSection {
	private Stimulus stimulus;
	private Baseline baseline;

	public Baseline getBaseline() {
		return baseline;
	}

	public Stimulus getStimulus() {
		return stimulus;
	}

	public boolean hasBaseline() {
		return baseline != null;
	}

	public boolean hasStimulus() {
		return stimulus != null;
	}

	public void setBaseline(Baseline baseline) {
		this.baseline = baseline;
	}

	public void setStimulus(Stimulus stimulus) {
		this.stimulus = stimulus;
	}
}
