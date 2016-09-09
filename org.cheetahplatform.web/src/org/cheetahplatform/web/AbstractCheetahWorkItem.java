package org.cheetahplatform.web;

import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.split;

import java.sql.SQLException;
import java.util.List;

import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.PpmInstanceDto;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.util.FileUtils;

public abstract class AbstractCheetahWorkItem implements ICheetahWorkItem {
	protected class SubjectPpmInstancePair {
		private SubjectDto subject;
		private PpmInstanceDto instance;

		public SubjectPpmInstancePair(SubjectDto subject, PpmInstanceDto instance) {
			super();
			this.subject = subject;
			this.instance = instance;
		}

		public PpmInstanceDto getInstance() {
			return instance;
		}

		public SubjectDto getSubject() {
			return subject;
		}
	}

	protected static final String LOCAL_TIMESTAMP_COLUMN_HEADER = "LocalTimeStamp";
	private static NotificationDao notificationDao = new NotificationDao();

	private long id;

	protected long userId;

	private String filename;

	protected String message;

	protected Long fileId;

	public AbstractCheetahWorkItem(long userId, Long fileId, String message) {
		this.userId = userId;
		this.message = message;
		this.fileId = fileId;
	}

	@Override
	public void cancel() {
		// don't do anything
	}

	public int getColumnIndex(String columnName, String fileHeader) {
		List<String> headerColumns = split(fileHeader, '\t');
		int timestampIndex = -1;

		for (int i = 0; i < headerColumns.size(); i++) {
			String column = headerColumns.get(i);
			if (column.equals(columnName)) {
				timestampIndex = i;
			}
		}
		return timestampIndex;
	}

	@Override
	public String getDisplayName() {
		if (fileId == null) {
			return message;
		}

		if (filename == null) {
			try {
				filename = new UserFileDao().getFile(fileId).getFilename();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return message + ": " + filename;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getUserId() {
		return userId;
	}

	protected void logErrorNotification(String message) throws SQLException {
		logNotification(message, NotificationDao.NOTIFICATION_ERROR);
	}

	protected void logNotification(String message, String type) throws SQLException {
		notificationDao.insertNotification(message, type, userId);
	}

	protected void logSuccessNotification(String message) throws SQLException {
		logNotification(message, NotificationDao.NOTIFICATION_SUCCESS);
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	protected String[] splitFileName(String fileName) throws SQLException {
		String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);
		String[] splittedFilename = fileNameWithoutExtension.split(CheetahWebConstants.FILENAME_PATTERN_SEPARATOR);
		// do not care about anything after the second one
		if (splittedFilename.length < 2) {
			logErrorNotification("Could not extract proband and process information. Please check the file name.");
			return null;
		}

		return splittedFilename;
	}
}
