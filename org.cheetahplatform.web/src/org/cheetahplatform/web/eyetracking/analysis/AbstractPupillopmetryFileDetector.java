package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public abstract class AbstractPupillopmetryFileDetector {

	private List<TrialDetectionNotification> notifications;

	public AbstractPupillopmetryFileDetector() {
		super();
		notifications = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	protected List<PupillometryFileLine> extractLinesToConsider(PupillometryFileLine line) {
		List<PupillometryFileLine> linesToCheck = new ArrayList<>();
		linesToCheck.add(line);
		List<PupillometryFileLine> collapsedLines = (List<PupillometryFileLine>) line.getMarking(PupillometryFile.COLLAPSED_COLUMNS);
		if (collapsedLines != null) {
			linesToCheck.addAll(collapsedLines);
		}

		return linesToCheck;
	}

	public List<TrialDetectionNotification> getNotifications() {
		return Collections.unmodifiableList(notifications);
	}

	protected void logErrorNotifcation(String message) {
		notifications.add(new TrialDetectionNotification(message, TrialDetectionNotification.TYPE_ERROR));
	}

	protected void logInfoNotifcation(String message) {
		notifications.add(new TrialDetectionNotification(message, TrialDetectionNotification.TYPE_INFO));
	}

	protected void logWarningNotifcation(String message) {
		notifications.add(new TrialDetectionNotification(message, TrialDetectionNotification.TYPE_WARNING));
	}
}