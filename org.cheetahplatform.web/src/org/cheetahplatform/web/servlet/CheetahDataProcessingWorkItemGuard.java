package org.cheetahplatform.web.servlet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.dto.TrialAnalysisReportableResultEntry;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.eclipse.core.runtime.Assert;

public class CheetahDataProcessingWorkItemGuard extends CheetahWorkItemGuard {
	private DataProcessingResultCollector resultCollector;

	public CheetahDataProcessingWorkItemGuard(int numberOfExpectedresults, long userId, DataProcessingResultCollector resultCollector) {
		super(numberOfExpectedresults, userId, "");
		this.resultCollector = resultCollector;
		Assert.isNotNull(resultCollector);
	}

	@Override
	protected void mergeResults(ReportableResult result, ReportableResult reportableResult) {
		for (Entry<String, List<ReportableResultEntry>> entry : result.getResults().entrySet()) {
			List<ReportableResultEntry> entriesToAdd = entry.getValue();
			for (ReportableResultEntry reportableResultEntry : entriesToAdd) {
				TrialAnalysisReportableResultEntry trialResultEntry = (TrialAnalysisReportableResultEntry) reportableResultEntry;
				if (reportableResult.containsEntry(entry.getKey(), trialResultEntry.getTrialNumber())) {
					throw new IllegalArgumentException("Found multiple data points for the same evalaution for the same trial! Key: "
							+ entry.getKey() + "; Trial: " + trialResultEntry.getTrialNumber());
				}
			}

			reportableResult.addResults(entry.getKey(), entriesToAdd);
		}
	}

	@Override
	public void postResults(Map<String, ReportableResult> collectedResults) {
		resultCollector.addResults(this, collectedResults);
	}
}
