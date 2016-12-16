package org.cheetahplatform.web.eyetracking;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.cheetahplatform.web.ICheetahWorkItem;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.eyetracking.analysis.DefaultReportableResultWriter;

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

	protected void mergeResults(ReportableResult result, ReportableResult reportableResult) {
		for (Entry<String, List<ReportableResultEntry>> entry : result.getResults().entrySet()) {
			List<ReportableResultEntry> resultEntriesForKey = entry.getValue();
			for (ReportableResultEntry reportableResultEntry : resultEntriesForKey) {
				if (!reportableResult.isResultDefined(entry.getKey())) {
					reportableResult.addResult(entry.getKey(), reportableResultEntry);
				} else {
					int fileCount = 1;
					String columnName = entry.getKey() + "_" + fileCount;
					while (reportableResult.isResultDefined(columnName)) {
						fileCount++;
						columnName = entry.getKey() + "_" + fileCount;
					}
					reportableResult.addResult(columnName, reportableResultEntry);
				}
			}

		}
	}

	public void postResults(Map<String, ReportableResult> collectedResults) {
		new DefaultReportableResultWriter().write(userId, collectedResults, filePrefix, resultFileComment);
	}

	protected void processResult(Map<String, ReportableResult> collectedResults, ReportableResult result) {
		ReportableResult reportableResult = collectedResults.get(result.getIdentifier());
		if (reportableResult == null) {
			reportableResult = new ReportableResult(result.getIdentifier());
			reportableResult.putAllResults(result.getResults());

			collectedResults.put(result.getIdentifier(), reportableResult);
		} else {
			mergeResults(result, reportableResult);
		}
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

		int numberOfCollectedResults = 0;
		while (numberOfCollectedResults < numberOfExpectedResults) {
			try {
				ReportableResult result = results.poll(60, TimeUnit.MINUTES);
				// The last work item I have been waiting for has been cancelled in the mean time -> done :)
				if (numberOfCollectedResults >= numberOfExpectedResults) {
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

				numberOfCollectedResults++;
				processResult(collectedResults, result);

			} catch (InterruptedException e) {
				// ignore - just retry
			}
		}

		postResults(collectedResults);
	}
}
