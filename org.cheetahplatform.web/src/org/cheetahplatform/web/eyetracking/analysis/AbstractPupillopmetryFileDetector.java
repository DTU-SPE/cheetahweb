package org.cheetahplatform.web.eyetracking.analysis;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public abstract class AbstractPupillopmetryFileDetector {

	private static NumberFormat TIME_FORMAT = new DecimalFormat("0.000");

	private List<TrialDetectionNotification> notifications;

	public AbstractPupillopmetryFileDetector() {
		super();
		notifications = new ArrayList<>();
	}

	protected void addAllNotification(Collection<TrialDetectionNotification> toAdd) {
		notifications.addAll(toAdd);
	}

	protected void addRelativeTime(PupillometryFileColumn relativeTimeColumn, PupillometryFileColumn timeStampColumn,
			List<PupillometryFileLine> linesInSegment, PupillometryFileLine lineToAddRelativetime) {
		double relativeTime = 0;
		if (!linesInSegment.isEmpty()) {
			long timestamp = lineToAddRelativetime.getLong(timeStampColumn);
			long startTimeStamp = linesInSegment.get(0).getLong(timeStampColumn);
			// convert nanoseconds to milliseconds for #671
			relativeTime = ((double) (timestamp - startTimeStamp)) / 1000;
		}

		lineToAddRelativetime.setValue(relativeTimeColumn, TIME_FORMAT.format(relativeTime));
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

	protected PupillometryFileColumn initializeColumn(PupillometryFile pupillometryFile, String columnName) throws IOException {
		PupillometryFileColumn column = null;
		PupillometryFileHeader header = pupillometryFile.getHeader();
		if (!header.hasColumn(columnName)) {
			column = pupillometryFile.appendColumn(columnName);
		} else {
			column = header.getColumn(columnName);
			pupillometryFile.clearColumn(column);
		}
		return column;
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