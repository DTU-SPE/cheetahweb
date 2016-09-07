package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DataProcessingDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.ExecuteDataProcessingRequest;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.cleaning.ExecuteDataProcessingWorkItem;

public class ExecuteDataProcessingServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -5117978614041617213L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		UserDao userDao = new UserDao();
		long userId = userDao.getUserId(connection, request);

		ExecuteDataProcessingRequest executeDataProcessingRequest = readJson(request, ExecuteDataProcessingRequest.class);
		Map<Long, DataProcessing> dataProcessings = new DataProcessingDao().selectDataProcessingForStudy(connection,
				executeDataProcessingRequest.getStudyId());
		DataProcessing processing = dataProcessings.get(executeDataProcessingRequest.getDataProcessingId());

		List<Long> fileIds = executeDataProcessingRequest.getFileIds();
		for (Long fileId : fileIds) {
			new ExecuteDataProcessingWorkItem(userId, fileId, processing);
		}
	}
}
