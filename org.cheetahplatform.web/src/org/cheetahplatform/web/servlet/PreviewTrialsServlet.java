package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT;
import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetectionNotification;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

/**
 * Servlet implementation class PreviewTrialsServlet
 */
public class PreviewTrialsServlet extends AbstractCheetahServlet {
	static class PreviewTrial {
		private String name;
		private List<String> scenes;

		public PreviewTrial(String name, List<String> scenes) {
			this.name = name;
			this.scenes = scenes;
		}

		public String getName() {
			return name;
		}

		public List<String> getScenes() {
			return scenes;
		}
	}

	static class PreviewTrialResponse {
		private List<PreviewTrial> trials;
		private List<TrialDetectionNotification> notifications;

		public PreviewTrialResponse() {
			this.trials = new ArrayList<>();
			this.notifications = new ArrayList<>();
		}

		public void addTrial(int number, List<String> scenes) {
			trials.add(new PreviewTrial("Trial " + number, scenes));
		}

		public List<TrialDetectionNotification> getNotifications() {
			return notifications;
		}

		public List<PreviewTrial> getTrials() {
			return trials;
		}

		public void setNotifications(List<TrialDetectionNotification> notifications) {
			this.notifications = notifications;
		}
	}

	private static final long serialVersionUID = 2561815294871921766L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		TrialDetector trialDetector = new TrialDetector(trialRequest.getFileId(), trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());
		PupillometryFile pupillometryFile = trialDetector.loadPupillometryFile();
		PupillometryFileColumn studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);
		PupillometryFileColumn studioEventColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT);

		TrialEvaluation trialEvaluation = trialDetector.detectTrials(false, false);
		List<Trial> trials = trialEvaluation.getTrials();

		PreviewTrialResponse trialResponse = new PreviewTrialResponse();
		for (Trial trial : trials) {
			List<String> scenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
			trialResponse.addTrial(trial.getTrialNumber(), scenes);
		}
		trialResponse.setNotifications(trialEvaluation.getNotifications());

		writeJson(response, trialResponse);
	}
}
