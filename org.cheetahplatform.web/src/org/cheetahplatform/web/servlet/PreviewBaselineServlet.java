package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputeTrialsResponse;
import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;

public class PreviewBaselineServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -5383148313035026287L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		TrialDetector trialDetector = new TrialDetector(trialRequest.getFileId(), trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());
		TrialEvaluation trialEvaluation = trialDetector.detectTrials();
		int baselineCount = 0;
		int stimulusCount = 0;
		for (Trial trial : trialEvaluation.getTrials()) {
			if (trial.hasStimulus()) {
				stimulusCount++;
			}

			if (trial.hasBaseline()) {
				baselineCount++;
			}
		}

		System.out.println("Trials: " + trialEvaluation.getTrials());
		System.out.println("Trials with stimulus: " + stimulusCount);
		System.out.println("Trials with baseline: " + baselineCount);

		ComputeTrialsResponse result = new ComputeTrialsResponse(trialEvaluation.getTrials().size(), stimulusCount, baselineCount);
		writeJson(response, result);
	}
}
