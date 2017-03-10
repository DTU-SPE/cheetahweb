package org.cheetahplatform.web.eyetracking.analysis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.eyetracking.CachedEyeTrackingData;
import org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource;
import org.cheetahplatform.web.eyetracking.EyeTrackingCache;
import org.cheetahplatform.web.eyetracking.EyeTrackingEntry;
import org.cheetahplatform.web.eyetracking.NoEyeTrackingDataException;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class AverageLoadForPpmInstanceAnalyzer extends AbstractAverageLoadAnalyzer {

	public AverageLoadForPpmInstanceAnalyzer(long ppmInstanceId, long userId) {
		super(ppmInstanceId, userId);
	}

	@Override
	public List<String> analyze() throws Exception {
		ProcessInstance processInstance = DatabasePromReader.readProcessInstance(ppmInstanceId,
				AbstractCheetahServlet.getDatabaseConnection());
		AuditTrailEntry firstEntry = processInstance.getEntries().get(0);
		long start = firstEntry.getTimestamp().getTime() * 1000;
		AuditTrailEntry lastEntry = processInstance.getEntries().get(processInstance.getEntries().size() - 1);
		long end = lastEntry.getTimestamp().getTime() * 1000;

		return calculateAverageLoadWithinTimeframe(start, end);
	}

	public List<String> calculateAverageLoadWithinTimeframe(long start, long end) throws Exception {
		List<CachedEyeTrackingData> cachedEyeTrackingData;
		try {
			cachedEyeTrackingData = EyeTrackingCache.INSTANCE.getForPpmInstance(ppmInstanceId, new DatabaseEyeTrackingSource());
		} catch (NoEyeTrackingDataException e) {
			new NotificationDao().insertNotification(e.getMessage(), NotificationDao.NOTIFICATION_ERROR, userId);
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<String>();
		for (CachedEyeTrackingData data : cachedEyeTrackingData) {
			List<EyeTrackingEntry> entries = data.getEntries();

			double sumLeftPupil = 0.0;
			long countLeftPupil = 0;

			double sumRightPupil = 0.0;
			long countRightPupil = 0;

			for (EyeTrackingEntry eyeTrackingEntry : entries) {
				long timestamp = eyeTrackingEntry.getTimestamp();
				if (timestamp >= start && timestamp <= end) {
					double leftPupil = eyeTrackingEntry.getLeftPupil();
					if (leftPupil > 0) {
						sumLeftPupil += leftPupil;
						countLeftPupil++;
					}

					double rightPupil = eyeTrackingEntry.getRightPupil();
					if (rightPupil > 0) {
						sumRightPupil += rightPupil;
						countRightPupil++;
					}
				}
			}

			double averageLoadLeftPupil = sumLeftPupil / countLeftPupil;
			double averageLoadRightPupil = sumRightPupil / countRightPupil;

			DecimalFormat format = new DecimalFormat("#.0000");
			String line = data.getLabel() + ";" + format.format(averageLoadLeftPupil) + ";" + format.format(averageLoadRightPupil);
			result.add(line);
		}
		return result;
	}

	@Override
	public String getName() {
		return "Average Load for PPM instances";
	}
}
