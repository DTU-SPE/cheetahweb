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
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.analysis.CleanDataConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingStep;
import org.cheetahplatform.web.eyetracking.analysis.TrialConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeConfiguration;
import org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem;
import org.cheetahplatform.web.eyetracking.cleaning.ComputeTrialsWorkItem;
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

		DataProcessingResultCollector resultCollector = new DataProcessingResultCollector(userId, processing);

		for (Long fileId : fileIds) {
			boolean firstAnalyzeStep = true;
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
				} else if (DataProcessingStep.DATA_PROCESSING_TYPE_ANALYZE.equals(dataProcessingStep.getType())) {
					if (firstAnalyzeStep) {
						firstAnalyzeStep = false;
						String trialComputationConfiguration = processing.getTrialComputationConfiguration();
						TrialConfiguration trialConfiguration = readJson(trialComputationConfiguration, TrialConfiguration.class);

						ComputeTrialsWorkItem computeTrialsWorkItem = new ComputeTrialsWorkItem(userId, fileId, trialConfiguration,
								processing.getDecimalSeparator(), processing.getTimestampColumn());
						dataProcessingWorkItem.addDataProcessingWorkItem(computeTrialsWorkItem);
					}

					AnalyzeConfiguration config = readJson(dataProcessingStep.getConfiguration(), AnalyzeConfiguration.class);
					if (!resultCollector.hasGuard(dataProcessingStep)) {
						resultCollector.register(dataProcessingStep,
								new CheetahDataProcessingWorkItemGuard(fileIds.size(), userId, resultCollector));
					}

					CheetahWorkItemGuard cheetahWorkItemGuard = resultCollector.get(dataProcessingStep);
					AnalyzeTrialsWorkItem analyzeTrialsWorkItem = new AnalyzeTrialsWorkItem(userId, fileId, config, cheetahWorkItemGuard,
							processing);
					dataProcessingWorkItem.addDataProcessingWorkItem(analyzeTrialsWorkItem);
				} else {
					throw new IllegalArgumentException("Unsupported data processing step of type " + dataProcessingStep.getType());
				}
			}

			CheetahWorker.schedule(dataProcessingWorkItem);
		}
	}
}
