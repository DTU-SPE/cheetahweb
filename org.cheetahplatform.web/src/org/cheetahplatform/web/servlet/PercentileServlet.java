package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.PercentileDto;
import org.cheetahplatform.web.dto.PercentilesDto;
import org.cheetahplatform.web.eyetracking.CachedEyeTrackingData;
import org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource;
import org.cheetahplatform.web.eyetracking.EyeTrackingCache;
import org.cheetahplatform.web.eyetracking.EyeTrackingEntry;

public class PercentileServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = -3938453204281307632L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		try {
			int percentile = Integer.parseInt(request.getParameter("percentile"));
			double minDuration = Double.parseDouble(request.getParameter("minDuration"));
			String mode = request.getParameter("mode");
			long dataId = Long.parseLong(request.getParameter("eyetrackingData"));

			PercentilesDto dto = new PercentilesDto();
			List<CachedEyeTrackingData> eyeTrackingData = EyeTrackingCache.INSTANCE.get(new DatabaseEyeTrackingSource(), dataId);
			CachedEyeTrackingData data = eyeTrackingData.get(0);
			if (mode.equals("slidingWindow")) {
				int duration = Integer.parseInt(request.getParameter("slidingWindowDuration"));
				data = data.computeSlidingWindow(duration);
				if (!data.areCachedValuesComputed()) {
					data.computeCachedValues();
				}
			}

			double cutOff = data.getPercentiles().get(percentile);
			EyeTrackingEntry start = null;
			List<PercentileDto> result = new ArrayList<PercentileDto>();
			List<EyeTrackingEntry> values = new ArrayList<EyeTrackingEntry>();
			double sum = 0;

			for (EyeTrackingEntry entry : data.getEntries()) {
				boolean aboveCutoff = entry.getAverage() > cutOff;

				if (aboveCutoff) {
					// mark the beginning of a phase
					if (start == null) {
						start = entry;
					}

					values.add(entry);
					sum += entry.getAverage();
				} else {
					if (start != null) {
						// check if duration is long enough
						long duration = entry.getTimestamp() - start.getTimestamp();
						if (duration > minDuration * 1000) {
							double average = sum / values.size();
							result.add(new PercentileDto(start.getTimestamp(), entry.getTimestamp(), average));
						}

						sum = 0;
						values.clear();
						start = null;
					}
				}
			}

			dto.setPercentiles(result);
			dto.setSessionStart(data.getSessionStart());
			writeJson(response, dto);
		} catch (

		Exception e) {
			writeJson(response, new Object());
		}
	}
}
