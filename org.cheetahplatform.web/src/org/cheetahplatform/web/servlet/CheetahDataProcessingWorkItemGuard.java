package org.cheetahplatform.web.servlet;

import java.util.Map;

import org.cheetahplatform.web.dto.ReportableResult;
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
	public void postResults(Map<String, ReportableResult> collectedResults) {
		resultCollector.addResults(this, collectedResults);
	}
}
