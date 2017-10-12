package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.SequencesOfPupillometryRequestLabeled;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuardMeasures;
import org.cheetahplatform.web.eyetracking.analysis.LabeledPhaseAverageLoadForTsvFileWorkItem;

/**
 * Servlet implementation class SequencesOfPupillometryServlet
 */
public class SequencesOfPupillometryServletLabeled extends AbstractCheetahServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		long userId = new UserDao().getUserId(connection, req);

		SequencesOfPupillometryRequestLabeled request = readJson(req, SequencesOfPupillometryRequestLabeled.class);
		System.out.println(request.getBaseline());
		List<Long> fileIds = request.getFileIds();
		System.out.println(request);
		CheetahWorkItemGuard guard = new CheetahWorkItemGuardMeasures(fileIds.size(), userId, "Measures_For_Labeled_Phases",
				"Calculated Measures For Labeled Phases from .tsv files.");
		for (Long fileId : fileIds) {
			LabeledPhaseAverageLoadForTsvFileWorkItem analyzer = new LabeledPhaseAverageLoadForTsvFileWorkItem(fileId,
					request.getLeftPupilColumn(), request.getRightPupilColumn(), request.getLabelList(), request.getLabelColumn(),
					request.getBaseline(), userId, guard);
			CheetahWorker.schedule(analyzer);
		}
	}

}
