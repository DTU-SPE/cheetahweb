package org.cheetahplatform.web.eyetracking;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.eyetracking.analysis.ReportableResultWriterForMeasures;

public class CheetahWorkItemGuardMeasures extends CheetahWorkItemGuard {

	public CheetahWorkItemGuardMeasures(int numberOfExpectedResults, long userId, String filePrefix) {
		super(numberOfExpectedResults, userId, filePrefix);
	}

	public CheetahWorkItemGuardMeasures(int numberOfExpectedResults, long userId, String filePrefix, String resultFileComment) {
		super(numberOfExpectedResults, userId, filePrefix, resultFileComment);
	}

	@Override
	protected void mergeResults(ReportableResult result, ReportableResult reportableResult) {
		for (Entry<String, List<ReportableResultEntry>> entry : result.getResults().entrySet()) {
			List<ReportableResultEntry> resultEntriesForKey = entry.getValue();
			for (ReportableResultEntry reportableResultEntry : resultEntriesForKey) {
				reportableResult.addResult(entry.getKey(), reportableResultEntry);

			}

		}
	}

	@Override
	public void postResults(Map<String, ReportableResult> collectedResults) {
		new ReportableResultWriterForMeasures().write(userId, collectedResults, filePrefix, resultFileComment);
	}
}
