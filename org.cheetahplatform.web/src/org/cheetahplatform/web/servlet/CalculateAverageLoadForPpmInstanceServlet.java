package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.CalculateAverageLoadForPpmInstanceRequest;
import org.cheetahplatform.web.eyetracking.analysis.AverageLoadForPpmInstanceAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.EyetrackingAnalysisWorkItem;
import org.cheetahplatform.web.eyetracking.analysis.IEyeTrackingDataAnalyzer;

public class CalculateAverageLoadForPpmInstanceServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -7186998996159022587L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		long userId = new UserDao().getUserId(connection, req);
		CalculateAverageLoadForPpmInstanceRequest request = readJson(req, CalculateAverageLoadForPpmInstanceRequest.class);

		List<IEyeTrackingDataAnalyzer> analyzers = new ArrayList<IEyeTrackingDataAnalyzer>();

		List<Long> ppmInstanceIds = request.getPpmInstanceIds();
		for (Long ppmInstanceId : ppmInstanceIds) {
			AverageLoadForPpmInstanceAnalyzer analyzer = new AverageLoadForPpmInstanceAnalyzer(ppmInstanceId, userId);
			analyzers.add(analyzer);
		}

		EyetrackingAnalysisWorkItem workItem = new EyetrackingAnalysisWorkItem(userId, analyzers);
		CheetahWorker.schedule(workItem);
	}
}
