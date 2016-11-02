package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.List;

public class TrialEvaluation {
	private List<Trial> trials;
	private List<TrialDetectionNotification> notifications;

	public TrialEvaluation(List<Trial> trials, List<TrialDetectionNotification> notifications) {
		this.trials = trials;
		this.notifications = notifications;
	}

	public TrialEvaluation(List<TrialDetectionNotification> notifications) {
		this.notifications = notifications;
		trials = new ArrayList<>();
	}

	public List<TrialDetectionNotification> getNotifications() {
		return notifications;
	}

	public List<Trial> getTrials() {
		return this.trials;
	}

	public void setNotifications(List<TrialDetectionNotification> notifications) {
		this.notifications = notifications;
	}

	public void setTrials(List<Trial> trials) {
		this.trials = trials;
	}
}
