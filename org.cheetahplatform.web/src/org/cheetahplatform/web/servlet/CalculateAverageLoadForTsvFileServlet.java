package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.CalculateAverageLoadForTsvFileRequest;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.analysis.AverageLoadForTsvFileWorkItem;

public class CalculateAverageLoadForTsvFileServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -7186998996159022587L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		long userId = new UserDao().getUserId(connection, req);
		CalculateAverageLoadForTsvFileRequest request = readJson(req, CalculateAverageLoadForTsvFileRequest.class);
		List<Long> fileIds = request.getFileIds();

		CheetahWorkItemGuard guard = new CheetahWorkItemGuard(fileIds.size(), userId, "Average_Cognitive_Load",
				"Calculated Average Load from .tsv files.");
		for (Long fileId : fileIds) {
			AverageLoadForTsvFileWorkItem analyzer = new AverageLoadForTsvFileWorkItem(fileId, request.getLeftPupilColumn(),
					request.getRightPupilColumn(), userId, guard);
			CheetahWorker.schedule(analyzer);
		}
	}
}
