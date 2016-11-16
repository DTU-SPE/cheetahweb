package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ReportableResult;

public class ReportableResultWriter {
	private static final String SUBJECT_COLUMN = "subject";
	private static final String SEPARATOR = ";";
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd_HH-mm");

	public void write(long userId, Map<String, ReportableResult> collectedResults, String filePrefix, String resultFileComment) {
		List<String> headers = new ArrayList<>();
		headers.add(SUBJECT_COLUMN);
		for (Entry<String, ReportableResult> entry : collectedResults.entrySet()) {
			Set<String> keys = entry.getValue().getDefinedKeys();
			for (String key : keys) {
				if (!headers.contains(key)) {
					headers.add(key);
				}
			}
		}
		Collections.sort(headers, Collator.getInstance());

		NotificationDao notificationDao = new NotificationDao();
		StringBuilder builder = new StringBuilder();
		for (String header : headers) {
			builder.append(header);
			builder.append(SEPARATOR);
		}
		builder.append("\n");

		for (Entry<String, ReportableResult> entry : collectedResults.entrySet()) {
			for (String key : headers) {
				if (key.equals(SUBJECT_COLUMN)) {
					builder.append(entry.getKey());
					builder.append(SEPARATOR);
					continue;
				}

				ReportableResult reportableResult = entry.getValue();
				String result = reportableResult.getResult(key);
				if (result != null) {
					builder.append(result);
				}
				builder.append(SEPARATOR);
			}
			builder.append("\n");
		}

		UserFileDao userFileDao = new UserFileDao();
		String fileName = filePrefix + "_" + DATE_FORMAT.format(new java.util.Date()) + ".csv";
		String relativePath = userFileDao.generateRelativePath(userId, fileName);
		String absolutePath = userFileDao.getAbsolutePath(relativePath);
		File file = new File(absolutePath);
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(builder.toString());
			writer.close();

			long fileId = userFileDao.insertUserFile(userId, fileName, relativePath, "application/octet-stream", resultFileComment, null,
					null, false, null);
			userFileDao.addTags(fileId, UserFileDao.TAG_RESULT);

			String message = "Analysis complete. Find the file '" + fileName + "' in your data section!";
			notificationDao.insertNotification(message, NotificationDao.NOTIFICATION_SUCCESS, userId);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				notificationDao.insertNotification("An error occured while saving the results file.", NotificationDao.NOTIFICATION_ERROR,
						userId);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
