package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;

public class ReportableResultWriterForMeasures extends AbstractReportableResultWriter {

	@Override
	public void write(long userId, Map<String, ReportableResult> collectedResults, String filePrefix, String resultFileComment) {
		List<String> headers = extractHeaders(collectedResults);
		StringBuilder builder = writeHeaders(headers);

		for (Entry<String, ReportableResult> entry : collectedResults.entrySet()) {
			ReportableResult value = entry.getValue();
			int entryCount = 0;
			Boolean addLinesToReport = true;
			while (addLinesToReport) {
				for (String key : headers) {
					if (key.equals(SUBJECT_COLUMN)) {
						builder.append(entry.getKey());
						builder.append(SEPARATOR);
						continue;
					}

					List<ReportableResultEntry> result = value.getResult(key);
					if (entryCount >= result.size()) {
						addLinesToReport = false;
						break;
					}

					if (!result.isEmpty()) {
						String result2 = result.get(entryCount).getResult();
						System.out.println(result2);
						builder.append(result2);
					}

					builder.append(SEPARATOR);
				}
				entryCount++;
				builder.append("\n");
			}
		}

		writeResultFile(userId, filePrefix, resultFileComment, builder.toString());
	}

}
