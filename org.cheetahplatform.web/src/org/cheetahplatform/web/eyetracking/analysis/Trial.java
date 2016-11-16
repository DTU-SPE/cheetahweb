package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trial extends AbstractPupillometryFileSection {
	private Stimulus stimulus;
	private Baseline baseline;
	private int trialNumber;
	private List<TrialDetectionNotification> notifications;

	public Trial() {
		// JSON
	}

	public Trial(int number) {
		super();
		trialNumber = number;
		notifications = new ArrayList<>();
	}

	public void addAllNotifications(List<TrialDetectionNotification> notifications) {
		this.notifications.addAll(notifications);
	}

	public Baseline getBaseline() {
		return baseline;
	}

	public List<TrialDetectionNotification> getNotifications() {
		return Collections.unmodifiableList(notifications);
	}

	public Stimulus getStimulus() {
		return stimulus;
	}

	public int getTrialNumber() {
		return trialNumber;
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

	public void setNotifications(List<TrialDetectionNotification> notifications) {
		this.notifications = notifications;
	}

	public void setStimulus(Stimulus stimulus) {
		this.stimulus = stimulus;
	}

	public void setTrialNumber(int trialNumber) {
		this.trialNumber = trialNumber;
	}
}
