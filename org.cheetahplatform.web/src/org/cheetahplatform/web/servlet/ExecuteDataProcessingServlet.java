package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.DataProcessingDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.ExecuteDataProcessingRequest;
import org.cheetahplatform.web.dto.FilterRequest;
import org.cheetahplatform.web.eyetracking.analysis.CleanDataConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingStep;
import org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem;
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
			ExecuteDataProcessingWorkItem dataProcessingWorkItem = new ExecuteDataProcessingWorkItem(userId, fileId, processing);

			for (DataProcessingStep dataProcessingStep : processing.getSteps()) {
				if (DataProcessingStep.DATA_PROCESSING_TYPE_CLEAN.equals(dataProcessingStep.getType())) {
					CleanDataConfiguration config = readJson(dataProcessingStep.getConfiguration(), CleanDataConfiguration.class);

					FilterRequest filterRequest = new FilterRequest();
					filterRequest.setParameters(config.getParameters());
					filterRequest.setDecimalSeparator(config.getDecimalSeparator());
					filterRequest.setFilters(config.getFilters());

					CleanPupillometryDataWorkItem cleanPupillometryDataWorkItem = new CleanPupillometryDataWorkItem(userId, filterRequest,
							fileId);
					dataProcessingWorkItem.addDataProcessingWorkItem(cleanPupillometryDataWorkItem);
				} else {
					throw new IllegalArgumentException("Unsupported data processing step of type " + dataProcessingStep.getType());
				}
			}

			CheetahWorker.schedule(dataProcessingWorkItem);
		}
	}
}
