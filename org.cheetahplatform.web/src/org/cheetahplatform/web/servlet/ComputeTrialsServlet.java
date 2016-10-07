package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputTrialsResponse;
import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;

public class ComputeTrialsServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -5383148313035026287L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		TrialDetector trialDetector = new TrialDetector(trialRequest.getFileId(), trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());
		List<Trial> trials = trialDetector.detectTrials();
		int baselineCount = 0;
		int stimulusCount = 0;
		for (Trial trial : trials) {
			if (trial.hasStimulus()) {
				stimulusCount++;
			}

			if (trial.hasBaseline()) {
				baselineCount++;
			}
		}

		System.out.println("Trials: " + trials.size());
		System.out.println("Trials with stimulus: " + stimulusCount);
		System.out.println("Trials with baseline: " + baselineCount);

		ComputTrialsResponse result = new ComputTrialsResponse(trials.size(), stimulusCount, baselineCount);
		writeJson(response, result);
	}
}
