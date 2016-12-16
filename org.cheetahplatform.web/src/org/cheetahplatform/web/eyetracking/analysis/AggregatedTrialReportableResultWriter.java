package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.dto.TrialAnalysisReportableResultEntry;

public class AggregatedTrialReportableResultWriter extends AbstractReportableResultWriter {

	private static final String TRIAL_COLUMN = "trial";

	@Override
	public void write(long userId, Map<String, ReportableResult> collectedResults, String filePrefix, String resultFileComment) {
		List<String> headers = extractHeaders(collectedResults, TRIAL_COLUMN);
		StringBuilder builder = writeHeaders(headers);

		Collection<ReportableResult> values = collectedResults.values();
		for (ReportableResult reportableResult : values) {
			String subject = reportableResult.getIdentifier();

			// build internal structure for printing
			Map<Integer, Map<String, TrialAnalysisReportableResultEntry>> cache = new HashMap<>();
			Map<String, List<ReportableResultEntry>> results = reportableResult.getResults();
			for (Entry<String, List<ReportableResultEntry>> resultEntry : results.entrySet()) {
				List<ReportableResultEntry> entries = resultEntry.getValue();
				for (ReportableResultEntry reportableResultEntry : entries) {
					TrialAnalysisReportableResultEntry casted = (TrialAnalysisReportableResultEntry) reportableResultEntry;

					if (!cache.containsKey(casted.getTrialNumber())) {
						cache.put(casted.getTrialNumber(), new HashMap<String, TrialAnalysisReportableResultEntry>());
					}

					cache.get(casted.getTrialNumber()).put(resultEntry.getKey(), casted);
				}
			}

			for (Entry<Integer, Map<String, TrialAnalysisReportableResultEntry>> trialEntry : cache.entrySet()) {
				for (String header : headers) {
					if (SUBJECT_COLUMN.equals(header)) {
						builder.append(subject);
					} else if (TRIAL_COLUMN.equals(header)) {
						builder.append(trialEntry.getKey());
					} else {
						TrialAnalysisReportableResultEntry reportableResultEntry = trialEntry.getValue().get(header);
						if (reportableResult != null) {
							builder.append(reportableResultEntry.getResult());
						}
					}
					builder.append(SEPARATOR);
				}
				builder.append("\n");
			}
		}

		writeResultFile(userId, filePrefix, resultFileComment, builder.toString());
	}
}
