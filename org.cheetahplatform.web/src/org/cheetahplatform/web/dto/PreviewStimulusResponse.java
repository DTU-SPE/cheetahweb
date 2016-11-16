package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetectionNotification;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewStimulusResponse {
	private List<PreviewStimulusDto> trials;
	private List<TrialDetectionNotification> notifications;

	public PreviewStimulusResponse() {
		this.trials = new ArrayList<>();
		this.notifications = new ArrayList<>();
	}

	public void addTrial(Trial trial, PupillometryFileColumn studioEventColumn, PupillometryFileColumn studioEventDataColumn) {
		trials.add(new PreviewStimulusDto(trial, studioEventColumn, studioEventDataColumn));
	}

	public List<TrialDetectionNotification> getNotifications() {
		return notifications;
	}

	public List<PreviewStimulusDto> getTrials() {
		return trials;
	}

	public void setNotifications(List<TrialDetectionNotification> notifications) {
		this.notifications = notifications;
	}
}