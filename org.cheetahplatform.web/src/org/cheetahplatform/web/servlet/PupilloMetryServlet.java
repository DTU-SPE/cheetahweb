package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.PupillometryDto;
import org.cheetahplatform.web.eyetracking.CachedEyeTrackingData;
import org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource;
import org.cheetahplatform.web.eyetracking.EyeTrackingCache;
import org.cheetahplatform.web.eyetracking.EyeTrackingEntry;

public class PupilloMetryServlet extends AbstractCheetahServlet {

	private static final int AVERAGE_NUMBER = 10;
	private static final int MAX_ENTRIES = 3000;

	private static final long serialVersionUID = 680265988208330039L;

	private List<EyeTrackingEntry> average(List<EyeTrackingEntry> toAverage) {
		List<EyeTrackingEntry> averaged = new ArrayList<EyeTrackingEntry>();
		double leftSum = 0;
		double rightSum = 0;
		int count = 0;
		if (toAverage.isEmpty()) {
			return averaged;
		}

		EyeTrackingEntry last = toAverage.get(0);

		for (EyeTrackingEntry current : toAverage) {
			leftSum += current.getLeftPupil();
			rightSum += current.getRightPupil();
			count++;

			if (count == AVERAGE_NUMBER) {
				EyeTrackingEntry tmp = new EyeTrackingEntry(last.getTimestamp(), leftSum / count, rightSum / count);
				averaged.add(tmp);
				last = current;
				count = 0;
				leftSum = 0;
				rightSum = 0;
			}
		}

		return averaged;
	}

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		List<CachedEyeTrackingData> cachedData = null;
		long sessionStart = Long.MAX_VALUE;

		try {
			String filesParameter = request.getParameter("files");
			if (filesParameter != null) {
				List<Long> files = new ArrayList<>();
				for (String fileAsString : filesParameter.split(",")) {
					files.add(Long.parseLong(fileAsString));
				}
				cachedData = EyeTrackingCache.INSTANCE.get(new DatabaseEyeTrackingSource(), files.toArray(new Long[files.size()]));
				for (CachedEyeTrackingData cachedEyeTrackingData : cachedData) {
					long current = cachedEyeTrackingData.getSessionStart();
					if (current < sessionStart) {
						sessionStart = current;
					}
				}
			} else {
				long ppmInstance = Long.parseLong(request.getParameter("ppmInstance"));
				cachedData = EyeTrackingCache.INSTANCE.getForPpmInstance(ppmInstance, new DatabaseEyeTrackingSource());
				sessionStart = getSessionStart(ppmInstance);
			}

			long timestamp = Long.parseLong(request.getParameter("start"));
			if (timestamp == -1) {
				timestamp = Long.MAX_VALUE;
				for (CachedEyeTrackingData data : cachedData) {
					long currentSessionStart = data.getSessionStart();
					if (currentSessionStart < timestamp) {
						timestamp = currentSessionStart;
					}
				}
			}

			int slidingWindowDuration = Integer.parseInt(request.getParameter("slidingWindowDuration"));
			List<PupillometryDto> results = new ArrayList<>();
			for (CachedEyeTrackingData data : cachedData) {
				List<EyeTrackingEntry> filtered = filter(timestamp, data);
				List<EyeTrackingEntry> averaged = average(filtered);

				long sessionEnd = data.getEntries().get(data.getEntries().size() - 1).getTimestamp();
				PupillometryDto result = new PupillometryDto(sessionStart, sessionEnd, averaged, data.getLabel(), data.getId());
				result.setPercentiles(data.getPercentiles());
				result.setMin(data.getMin());
				result.setMax(data.getMax());

				// add sliding window if requested
				if (slidingWindowDuration != -1) {
					CachedEyeTrackingData slidingWindow = data.computeSlidingWindow(slidingWindowDuration);
					List<EyeTrackingEntry> filteredSlidingWindow = average(filter(timestamp, slidingWindow));
					result.setSlidingWindow(filteredSlidingWindow);
				}
				results.add(result);
			}

			writeJson(response, results);
		} catch (Exception e) {
			e.printStackTrace();
			writeJson(response, new ArrayList<Object>());
		}
	}

	private List<EyeTrackingEntry> filter(long timestamp, CachedEyeTrackingData data) {
		List<EyeTrackingEntry> filtered = new ArrayList<EyeTrackingEntry>();

		for (EyeTrackingEntry entry : data.getEntries()) {
			if (entry.getTimestamp() < timestamp) {
				continue;
			}

			if (entry.getTimestamp() > timestamp + 10 * 1000 * 1000) {
				continue;
			}

			filtered.add(entry);
			if (filtered.size() >= MAX_ENTRIES) {
				break;
			}
		}
		return filtered;
	}
}
