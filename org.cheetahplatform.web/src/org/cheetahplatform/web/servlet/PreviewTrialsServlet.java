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
import org.cheetahplatform.web.eyetracking.analysis.TrialDetector;
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

		public PreviewTrialResponse() {
			this.trials = new ArrayList<>();
		}

		public void addTrial(int number, List<String> scenes) {
			trials.add(new PreviewTrial("Trial " + number, scenes));
		}

		public List<PreviewTrial> getTrials() {
			return trials;
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

		List<Trial> trials = trialDetector.splitFileIntoTrials(pupillometryFile);
		PreviewTrialResponse trialResponse = new PreviewTrialResponse();
		for (int i = 0; i < trials.size(); i++) {
			Trial trial = trials.get(i);
			List<String> scenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
			trialResponse.addTrial(i + 1, scenes);
		}

		writeJson(response, trialResponse);
	}

}
