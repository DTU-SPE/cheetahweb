package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ReportableResult {
	private String identifier;
	private Map<String, List<ReportableResultEntry>> results;

	public ReportableResult(String identifier) {
		this.identifier = identifier;
		results = new HashMap<>();
	}

	public void addAllResults(Map<String, TrialAnalysisReportableResultEntry> resultsToAdd) {
		Set<Entry<String, TrialAnalysisReportableResultEntry>> entrySet = resultsToAdd.entrySet();
		for (Entry<String, TrialAnalysisReportableResultEntry> entry : entrySet) {
			if (!results.containsKey(entry.getKey())) {
				results.put(entry.getKey(), new ArrayList<ReportableResultEntry>());
			}

			results.get(entry.getKey()).add(entry.getValue());
		}
	}

	public void addResult(String key, ReportableResultEntry entry) {
		if (!results.containsKey(key)) {
			results.put(key, new ArrayList<ReportableResultEntry>());
		}

		results.get(key).add(entry);
	}

	public void addResults(String key, List<ReportableResultEntry> entries) {
		if (!results.containsKey(key)) {
			results.put(key, new ArrayList<ReportableResultEntry>());
		}

		results.get(key).addAll(entries);
	}

	public boolean containsEntry(String key, int trialNumber) {
		List<ReportableResultEntry> existingResults = results.get(key);
		if (existingResults == null || existingResults.isEmpty()) {
			return false;
		}

		for (ReportableResultEntry reportableResultEntry : existingResults) {
			if (reportableResultEntry.isSameResult(trialNumber)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getDefinedKeys() {
		return results.keySet();
	}

	public String getIdentifier() {
		return identifier;
	}

	public List<ReportableResultEntry> getResult(String key) {
		List<ReportableResultEntry> list = results.get(key);
		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	public Map<String, List<ReportableResultEntry>> getResults() {
		return Collections.unmodifiableMap(results);
	}

	public boolean isResultDefined(String key) {
		return results.containsKey(key);
	}

	public void putAllResults(Map<String, List<ReportableResultEntry>> toInsert) {
		results.putAll(toInsert);
	}
}
