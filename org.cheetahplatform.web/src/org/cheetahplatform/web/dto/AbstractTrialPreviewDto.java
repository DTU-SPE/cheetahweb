package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetectionNotification;

public class AbstractTrialPreviewDto {
	protected String name;
	protected List<TrialPreviewSceneDto> scenes;
	protected List<TrialDetectionNotification> notifications;

	public AbstractTrialPreviewDto() {
		super();
	}

	public AbstractTrialPreviewDto(Trial trial) {
		this.name = "Trial " + trial.getTrialNumber();
		this.scenes = new ArrayList<>();
		this.notifications = trial.getNotifications();
	}

	public String getName() {
		return name;
	}

	public List<TrialDetectionNotification> getNotifications() {
		return notifications;
	}

	public List<TrialPreviewSceneDto> getScenes() {
		return scenes;
	}

}