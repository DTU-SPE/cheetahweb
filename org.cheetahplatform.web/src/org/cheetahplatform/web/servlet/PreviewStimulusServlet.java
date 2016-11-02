package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT;
import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.dto.PreviewStimulusResponse;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewStimulusServlet extends AbstractCheetahServlet {
	static final long serialVersionUID = -3833268302176793480L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		TrialDetector trialDetector = new TrialDetector(trialRequest.getFileId(), trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());
		PupillometryFile pupillometryFile = trialDetector.loadPupillometryFile();
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);
		PupillometryFileColumn studioEventColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT);

		TrialEvaluation trialEvaluation = trialDetector.detectTrials(true, false);
		List<Trial> trials = trialEvaluation.getTrials();
		PreviewStimulusResponse stimulusResponse = new PreviewStimulusResponse();

		for (Trial trial : trials) {
			stimulusResponse.addTrial(trial, studioEventColumn, studioEventDataColumn);
		}

		stimulusResponse.setNotifications(trialEvaluation.getNotifications());

		writeJson(response, stimulusResponse);
	}
}
