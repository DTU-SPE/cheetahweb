package org.cheetahplatform.web.eyetracking;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.cheetahplatform.web.ICheetahWorkItem;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.eyetracking.analysis.ReportableResultWriter;

public class CheetahWorkItemGuard extends Thread {

	private BlockingQueue<ReportableResult> results;
	private int numberOfExpectedResults;
	private long userId;
	private String filePrefix;
	private String resultFileComment;

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

	public void postResults(Map<String, ReportableResult> collectedResults) {
		new ReportableResultWriter().write(userId, collectedResults, filePrefix, resultFileComment);
	}

	public synchronized void reportResult(ReportableResult reportableResult) {
		if (!isAlive()) {
			start();
		}
		results.add(reportableResult);
	}

	@Override
	public void run() {
		NotificationDao notificationDao = new NotificationDao();

		Map<String, ReportableResult> collectedResults = new HashMap<>();

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

		postResults(collectedResults);
	}
}
