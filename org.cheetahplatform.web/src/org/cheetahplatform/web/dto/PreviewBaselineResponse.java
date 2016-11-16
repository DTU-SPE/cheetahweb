package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.TrialDetectionNotification;

public class PreviewBaselineResponse {
	private int numberOfTrials;
	private int numberOfTrialsWithStimulus;
	private int numberOfTrialsWithBaseline;
	private List<PreviewBaselineDto> preview;
	private List<TrialDetectionNotification> notifications;

	public PreviewBaselineResponse() {
		preview = new ArrayList<>();
		notifications = new ArrayList<>();
	}

	public PreviewBaselineResponse(int numberOfTrials, int numberOfTrialsWithStimulus, int numberOfTrialsWithBaseline) {
		this();
		this.numberOfTrials = numberOfTrials;
		this.numberOfTrialsWithStimulus = numberOfTrialsWithStimulus;
		this.numberOfTrialsWithBaseline = numberOfTrialsWithBaseline;
	}

	public void addTrialPreview(PreviewBaselineDto preview) {
		this.preview.add(preview);
	}

	public List<TrialDetectionNotification> getNotifications() {
		return notifications;
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

	public List<PreviewBaselineDto> getPreview() {
		return preview;
	}

	public void setNotifications(List<TrialDetectionNotification> notifications) {
		this.notifications = notifications;
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

	public void setPreview(List<PreviewBaselineDto> preview) {
		this.preview = preview;
	}
}
