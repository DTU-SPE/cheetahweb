package org.cheetahplatform.web.dto;

public class ComputTrialsResponse {
	private int numberOfTrials;
	private int numberOfTrialsWithStimulus;
	private int numberOfTrialsWithBaseline;

	public ComputTrialsResponse() {
		// Json
	}

	public ComputTrialsResponse(int numberOfTrials, int numberOfTrialsWithStimulus, int numberOfTrialsWithBaseline) {
		super();
		this.numberOfTrials = numberOfTrials;
		this.numberOfTrialsWithStimulus = numberOfTrialsWithStimulus;
		this.numberOfTrialsWithBaseline = numberOfTrialsWithBaseline;
	}

	public int getNumberOfTrials() {
		return numberOfTrials;
	}

	public int getNumberOfTrialsWithBaseline() {
		return numberOfTrialsWithBaseline;
	}

	public int getNumberOfTrialsWithStimulus() {
		return numberOfTrialsWithStimulus;
	}

	public void setNumberOfTrials(int numberOfTrials) {
		this.numberOfTrials = numberOfTrials;
	}

	public void setNumberOfTrialsWithBaseline(int numberOfTrialsWithBaseline) {
		this.numberOfTrialsWithBaseline = numberOfTrialsWithBaseline;
	}

	public void setNumberOfTrialsWithStimulus(int numberOfTrialsWithStimulus) {
		this.numberOfTrialsWithStimulus = numberOfTrialsWithStimulus;
	}
}
