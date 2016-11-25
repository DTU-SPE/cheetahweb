package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT;
import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.dto.PreviewBaselineDto;
import org.cheetahplatform.web.dto.PreviewBaselineResponse;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.DefaultTrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewBaselineServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = -5383148313035026287L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		DefaultTrialDetector trialDetector = new DefaultTrialDetector(trialRequest.getFileId(), trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());

		PupillometryFile pupillometryFile = trialDetector.loadPupillometryFile();
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);
		PupillometryFileColumn studioEventColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT);

		TrialEvaluation trialEvaluation = trialDetector.detectTrials();
		int baselineCount = 0;
		int stimulusCount = 0;
		List<Trial> trials = trialEvaluation.getTrials();
		PreviewBaselineResponse result = new PreviewBaselineResponse();

		for (Trial trial : trials) {
			PreviewBaselineDto preview = new PreviewBaselineDto(trial, studioEventColumn, studioEventDataColumn);
			result.addTrialPreview(preview);

			if (trial.hasStimulus()) {
				stimulusCount++;
			}

			if (trial.hasBaseline()) {
				baselineCount++;
			}
		}

		result.setNotifications(trialEvaluation.getNotifications());
		result.setNumberOfTrials(trialEvaluation.getTrials().size());
		result.setNumberOfTrialsWithStimulus(stimulusCount);
		result.setNumberOfTrialsWithBaseline(baselineCount);
		writeJson(response, result);
	}
}
