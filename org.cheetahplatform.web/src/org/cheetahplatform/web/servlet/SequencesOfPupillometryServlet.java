package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.SequencesOfPupillometryRequest;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuardMeasures;
import org.cheetahplatform.web.eyetracking.analysis.PhaseAverageLoadForTsvFileWorkItem;

/**
 * Servlet implementation class SequencesOfPupillometryServlet
 */
public class SequencesOfPupillometryServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		long userId = new UserDao().getUserId(connection, req);

		SequencesOfPupillometryRequest request = readJson(req, SequencesOfPupillometryRequest.class);
		System.out.println(request.getBaseline());
		List<Long> fileIds = request.getFileIds();
		System.out.println(request);
		CheetahWorkItemGuard guard = new CheetahWorkItemGuardMeasures(fileIds.size(), userId, "Measures_For_Phases",
				"Calculated Measures For Phases from .tsv files.");
		for (Long fileId : fileIds) {
			PhaseAverageLoadForTsvFileWorkItem analyzer = new PhaseAverageLoadForTsvFileWorkItem(fileId, request.getLeftPupilColumn(),
					request.getRightPupilColumn(), request.getTimeSlots(), request.getTimeStampsColumn(), request.getBaseline(), userId,
					guard);
			CheetahWorker.schedule(analyzer);
		}
	}

}
