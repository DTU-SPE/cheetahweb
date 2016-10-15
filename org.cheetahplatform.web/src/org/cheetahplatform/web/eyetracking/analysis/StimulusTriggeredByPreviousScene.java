package org.cheetahplatform.web.eyetracking.analysis;

/**
 * Stimulus detection that is based on a fixed scene before the actual stimulus. In other words, the stimulus is detected on the scene that
 * precedes the stimulus.
 *
 * @author stefan.zugal
 *
 */
public class StimulusTriggeredByPreviousScene extends StimulusConfiguration {
	/**
	 * The scene that precedes the actual stimulus.
	 */
	private String precedesStimulus;

	public String getPrecedesStimulus() {
		return precedesStimulus;
	}

	public void setPrecedesStimulus(String precedesStimulus) {
		this.precedesStimulus = precedesStimulus;
	}

}
