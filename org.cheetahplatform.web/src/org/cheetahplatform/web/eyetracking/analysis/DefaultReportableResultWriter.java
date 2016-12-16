package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;

public class DefaultReportableResultWriter extends AbstractReportableResultWriter {

	@Override
	public void write(long userId, Map<String, ReportableResult> collectedResults, String filePrefix, String resultFileComment) {
		List<String> headers = extractHeaders(collectedResults);
		StringBuilder builder = writeHeaders(headers);

		for (Entry<String, ReportableResult> entry : collectedResults.entrySet()) {
			ReportableResult value = entry.getValue();
			for (String key : headers) {
				if (key.equals(SUBJECT_COLUMN)) {
					builder.append(entry.getKey());
					builder.append(SEPARATOR);
					continue;
				}

				List<ReportableResultEntry> result = value.getResult(key);
				if (result.size() > 1) {
					throw new IllegalStateException("Found multiple results for the same key for the same subject. Key: " + key);
				}

				if (!result.isEmpty()) {
					builder.append(result.get(0).getResult());
				}

				builder.append(SEPARATOR);
			}
			builder.append("\n");
		}

		writeResultFile(userId, filePrefix, resultFileComment, builder.toString());
	}
}
