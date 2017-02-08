package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.dto.PreviewTrialResponse;
import org.cheetahplatform.web.eyetracking.analysis.CachedTrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.DefaultTrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

/**
 * Servlet implementation class PreviewTrialsServlet
 */
public class PreviewTrialsServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 2561815294871921766L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		DefaultTrialDetector trialDetector = new CachedTrialDetector(trialRequest.getFileId(), null, trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());
		PupillometryFile pupillometryFile = trialDetector.loadPupillometryFile();
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader()
				.getColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_STUDIO_EVENT_DATA);
		PupillometryFileColumn studioEventColumn = pupillometryFile.getHeader().getColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_STUDIO_EVENT);

		TrialEvaluation trialEvaluation = trialDetector.detectTrials(false, false, false);
		List<Trial> trials = trialEvaluation.getTrials();

		PreviewTrialResponse trialResponse = new PreviewTrialResponse();
		for (Trial trial : trials) {
			trialResponse.addTrial(trial, studioEventColumn, studioEventDataColumn);
		}
		trialResponse.setNotifications(trialEvaluation.getNotifications());

		writeJson(response, trialResponse);
	}
}
