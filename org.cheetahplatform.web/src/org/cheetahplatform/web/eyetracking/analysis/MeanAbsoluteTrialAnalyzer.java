package org.cheetahplatform.web.eyetracking.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileUtils;

public class MeanAbsoluteTrialAnalyzer extends AbstractTrialAnalyzer {
	public MeanAbsoluteTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing) {
		super(config, processing, AnalyzeStepType.MEAN_ABSOLUTE.getName());
	}

	@Override
	public Map<String, String> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) throws Exception {
		PupillometryFileColumn leftPupilColumn = pupillometryFile.getHeader().getColumn(dataProcessing.getLeftPupilColumn());
		PupillometryFileColumn rightPupilColumn = pupillometryFile.getHeader().getColumn(dataProcessing.getRightPupilColumn());

		Map<String, String> results = new HashMap<>();

		List<Trial> trials = trialEvaluation.getTrials();
		for (Trial trial : trials) {
			Stimulus stimulus = trial.getStimulus();
			if (stimulus == null) {
				continue;
			}

			List<PupillometryFileLine> lines = stimulus.getLines();
			if (lines.isEmpty()) {
				continue;
			}

			double[] leftPupils = PupillometryFileUtils.getPupilValues(lines, leftPupilColumn, false);
			double leftMean = new Mean().evaluate(leftPupils);
			String trialId = trial.getIdentifier();
			results.put(trialId + RESULT_SEPARATOR + AnalyzeStepType.MEAN_ABSOLUTE.getId() + RESULT_SEPARATOR + PUPIL_LEFT,
					String.valueOf(leftMean));

			double[] rightPupils = PupillometryFileUtils.getPupilValues(lines, rightPupilColumn, false);
			double rightMean = new Mean().evaluate(rightPupils);
			results.put(trialId + RESULT_SEPARATOR + AnalyzeStepType.MEAN_ABSOLUTE.getId() + RESULT_SEPARATOR + PUPIL_RIGHT,
					String.valueOf(rightMean));

			double average = (leftMean + rightMean) / 2;
			results.put(trialId + RESULT_SEPARATOR + AnalyzeStepType.MEAN_ABSOLUTE.getId() + RESULT_SEPARATOR + PUPIL_AVERAGE,
					String.valueOf(average));
		}

		return results;
	}
}
