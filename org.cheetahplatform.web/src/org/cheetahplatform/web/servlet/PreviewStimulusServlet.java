package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT;
import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetectionNotification;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewStimulusServlet extends AbstractCheetahServlet {
	class PreviewStimulus {
		private String name;
		private List<SceneOrStimulus> scenes;
		private List<TrialDetectionNotification> notifications;

		public PreviewStimulus(Trial trial, List<String> scenes, Stimulus stimulus) {
			this.name = "Trial " + trial.getTrialNumber();
			this.scenes = new ArrayList<>();
			this.notifications = trial.getNotifications();

			List<String> stimulusScenes = Collections.emptyList();
			if (stimulus != null) {
				stimulusScenes = stimulus.computeScenes(studioEventColumn, studioEventDataColumn);
			}

			for (String scene : scenes) {
				boolean isStimulusScene = stimulusScenes.contains(scene);
				if (isStimulusScene) {
					if (stimulusScenes.indexOf(scene) == 0) {
						this.scenes.add(new SceneOrStimulus("marker", "Start of stimulus"));
					}
					this.scenes.add(new SceneOrStimulus("stimulus", scene));
					if (stimulusScenes.indexOf(scene) == stimulusScenes.size() - 1) {
						this.scenes.add(new SceneOrStimulus("marker", "End of stimulus"));
					}
				} else {
					this.scenes.add(new SceneOrStimulus("scene", scene));
				}
			}
		}

		public String getName() {
			return name;
		}

		public List<TrialDetectionNotification> getNotifications() {
			return notifications;
		}

		public List<SceneOrStimulus> getScenes() {
			return scenes;
		}
	}

	class PreviewStimulusResponse {
		private List<PreviewStimulus> trials;
		private List<TrialDetectionNotification> notifications;

		public PreviewStimulusResponse() {
			this.trials = new ArrayList<>();
			this.notifications = new ArrayList<>();
		}

		public void addTrial(Trial trial, List<String> scenes, Stimulus stimulus) {
			trials.add(new PreviewStimulus(trial, scenes, stimulus));
		}

		public List<TrialDetectionNotification> getNotifications() {
			return notifications;
		}

		public List<PreviewStimulus> getTrials() {
			return trials;
		}

		public void setNotifications(List<TrialDetectionNotification> notifications) {
			this.notifications = notifications;
		}
	}

	static class SceneOrStimulus {
		private String type;
		private String name;

		public SceneOrStimulus(String type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}
	}

	static final long serialVersionUID = -3833268302176793480L;

	private PupillometryFileColumn studioEventDataColumn;
	private PupillometryFileColumn studioEventColumn;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeTrialsRequest trialRequest = readJson(request, ComputeTrialsRequest.class);
		TrialDetector trialDetector = new TrialDetector(trialRequest.getFileId(), trialRequest.getConfig(),
				trialRequest.getDecimalSeparator(), trialRequest.getTimestampColumn());
		PupillometryFile pupillometryFile = trialDetector.loadPupillometryFile();
		studioEventDataColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA);
		studioEventColumn = pupillometryFile.getHeader().getColumn(STUDIO_EVENT);

		TrialEvaluation trialEvaluation = trialDetector.detectTrials(true, false);
		List<Trial> trials = trialEvaluation.getTrials();
		PreviewStimulusResponse stimulusResponse = new PreviewStimulusResponse();

		for (Trial trial : trials) {
			List<String> scenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
			Stimulus stimulus = trial.getStimulus();
			stimulusResponse.addTrial(trial, scenes, stimulus);
		}

		stimulusResponse.setNotifications(trialEvaluation.getNotifications());

		writeJson(response, stimulusResponse);
	}
}
