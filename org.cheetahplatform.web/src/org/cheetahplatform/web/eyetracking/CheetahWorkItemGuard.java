package org.cheetahplatform.web.eyetracking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.cheetahplatform.web.ICheetahWorkItem;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ReportableResult;

public class CheetahWorkItemGuard extends Thread {
	private static final String SUBJECT_COLUMN = "subject";
	private static final String SEPARATOR = ";";
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd_HH-mm");
	private BlockingQueue<ReportableResult> results;
	private int numberOfExpectedResults;
	private long userId;
	private String filePrefix;
	private String resultFileComment;;

	public CheetahWorkItemGuard(int numberOfExpectedResults, long userId, String filePrefix) {
		this(numberOfExpectedResults, userId, filePrefix, "");
	}

	public CheetahWorkItemGuard(int numberOfExpectedResults, long userId, String filePrefix, String resultFileComment) {
		this.numberOfExpectedResults = numberOfExpectedResults;
		this.userId = userId;
		this.filePrefix = filePrefix;
		results = new ArrayBlockingQueue<>(numberOfExpectedResults);
		this.resultFileComment = resultFileComment;
	}

	public void canceled(ICheetahWorkItem averageLoadForTsvFileWorkItem) {
		numberOfExpectedResults--;
	}

	public void reportResult(ReportableResult reportableResult) {
		if (!isAlive()) {
			start();
		}
		results.add(reportableResult);
	}

	@Override
	public void run() {
		NotificationDao notificationDao = new NotificationDao();

		Map<String, ReportableResult> collectedResults = new HashMap<>();
		List<String> headers = new ArrayList<>();
		headers.add(SUBJECT_COLUMN);

		int i = 0;
		while (i < numberOfExpectedResults) {
			try {
				ReportableResult result = results.poll(60, TimeUnit.MINUTES);
				// The last work item I have been waiting for has been cancelled in the mean time -> done :)
				if (i >= numberOfExpectedResults) {
					break;
				}

				if (result == null) {
					try {
						notificationDao.insertNotification(
								"I have been waiting quite some time for the results... I guess something went terribly wrong...",
								NotificationDao.NOTIFICATION_ERROR, userId);
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
				}

				i++;
				ReportableResult reportableResult = collectedResults.get(result.getIdentifier());
				if (reportableResult == null) {
					reportableResult = new ReportableResult(result.getIdentifier());
					reportableResult.addAllResults(result.getResults());

					collectedResults.put(result.getIdentifier(), reportableResult);
				} else {
					for (Entry<String, String> entry : result.getResults().entrySet()) {
						if (!reportableResult.isResultDefined(entry.getKey())) {
							reportableResult.addResult(entry.getKey(), entry.getValue());
						} else {
							int fileCount = 1;
							String columnName = entry.getKey() + "_" + fileCount;
							while (reportableResult.isResultDefined(columnName)) {
								fileCount++;
								columnName = entry.getKey() + "_" + fileCount;
							}
							reportableResult.addResult(columnName, entry.getValue());
						}
					}
				}

			} catch (InterruptedException e) {
				// ignore - just retry
			}
		}

		for (Entry<String, ReportableResult> entry : collectedResults.entrySet()) {
			Set<String> keys = entry.getValue().getDefinedKeys();
			for (String key : keys) {
				if (!headers.contains(key)) {
					headers.add(key);
				}
			}
		}
		Collections.sort(headers, Collator.getInstance());

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
