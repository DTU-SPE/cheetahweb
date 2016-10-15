package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT;
import static org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.ComputeTrialsRequest;
import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

/**
 * Servlet implementation class PreviewStimulusServlet
 */
public class PreviewStimulusServlet extends AbstractCheetahServlet {
	class PreviewStimulus {
		private String name;
		private List<SceneOrStimulus> scenes;

		public PreviewStimulus(String name, List<String> scenes, Stimulus stimulus) {
			this.name = name;
			this.scenes = new ArrayList<>();

			String stimulusScene = null;
			if (stimulus != null) {
				List<String> stimulusScenes = stimulus.computeScenes(studioEventColumn, studioEventDataColumn);
				if (stimulusScenes.size() > 1) {
					throw new RuntimeException("Expected only one scene for the stimulus");
				}
				stimulusScene = stimulusScenes.get(0);
			}

			for (String scene : scenes) {
				boolean isStimulusScene = scene.equals(stimulusScene);
				if (isStimulusScene) {
					this.scenes.add(new SceneOrStimulus("marker", "Start of stimulus"));
					this.scenes.add(new SceneOrStimulus("stimulus", scene));
					this.scenes.add(new SceneOrStimulus("marker", "End of stimulus"));
				} else {
					this.scenes.add(new SceneOrStimulus("scene", scene));
				}
			}
		}

		public String getName() {
			return name;
		}

		public List<SceneOrStimulus> getScenes() {
			return scenes;
		}
	}

	class PreviewStimulusResponse {
		private List<PreviewStimulus> trials;

		public PreviewStimulusResponse() {
			this.trials = new ArrayList<>();
		}

		public void addTrial(int number, List<String> scenes, Stimulus stimulus) {
			trials.add(new PreviewStimulus("Trial " + number, scenes, stimulus));
		}

		public List<PreviewStimulus> getTrials() {
			return trials;
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

		List<Trial> trials = trialDetector.detectTrials(true, false);
		PreviewStimulusResponse stimulusResponse = new PreviewStimulusResponse();
		for (int i = 0; i < trials.size(); i++) {
			Trial trial = trials.get(i);
			List<String> scenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
			Stimulus stimulus = trial.getStimulus();
			stimulusResponse.addTrial(i + 1, scenes, stimulus);
		}

		writeJson(response, stimulusResponse);
	}
}
