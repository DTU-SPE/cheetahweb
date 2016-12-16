package org.cheetahplatform.web.servlet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.analysis.AggregatedTrialReportableResultWriter;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingStep;

public class DataProcessingResultCollector {
	private Map<DataProcessingStep, CheetahWorkItemGuard> guards = new HashMap<>();
	private Map<String, ReportableResult> resultsForSubject = new HashMap<>();
	private Set<CheetahWorkItemGuard> completedGuards = new HashSet<>();

	private long userId;
	private DataProcessing dataProcessing;

	public DataProcessingResultCollector(long userId, DataProcessing dataProcessing) {
		super();
		this.userId = userId;
		this.dataProcessing = dataProcessing;
	}

	public synchronized void addResults(CheetahDataProcessingWorkItemGuard guard, Map<String, ReportableResult> collectedResults) {

		for (Entry<String, ReportableResult> entry : collectedResults.entrySet()) {
			if (!resultsForSubject.containsKey(entry.getKey())) {
				resultsForSubject.put(entry.getKey(), entry.getValue());
				continue;
			}

			ReportableResult reportableResult = resultsForSubject.get(entry.getKey());
			reportableResult.putAllResults(entry.getValue().getResults());
		}

		completedGuards.add(guard);

		if (completedGuards.size() == guards.size()) {
			new AggregatedTrialReportableResultWriter().write(userId, resultsForSubject, dataProcessing.getName(),
					"Executed data processing " + dataProcessing.getName());
		}
	}

	public CheetahWorkItemGuard get(DataProcessingStep dataProcessingStep) {
		return guards.get(dataProcessingStep);
	}

	public boolean hasGuard(DataProcessingStep dataProcessingStep) {
		return guards.containsKey(dataProcessingStep);
	}

	public void register(DataProcessingStep dataProcessingStep, CheetahWorkItemGuard cheetahWorkItemGuard) {
		if (hasGuard(dataProcessingStep)) {
			throw new IllegalStateException("Cannot register multiple guards for a single data processing step.");
		}
		guards.put(dataProcessingStep, cheetahWorkItemGuard);
	}
}
