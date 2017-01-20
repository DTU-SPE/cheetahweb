package org.cheetahplatform.web.eyetracking.analysis.steps;

import static org.cheetahplatform.web.eyetracking.cleaning.SubstitutePupilFilter.MISSING_BOTH;
import static org.cheetahplatform.web.eyetracking.cleaning.SubstitutePupilFilter.MISSING_COLUMN;
import static org.cheetahplatform.web.eyetracking.cleaning.SubstitutePupilFilter.MISSING_LEFT;
import static org.cheetahplatform.web.eyetracking.cleaning.SubstitutePupilFilter.MISSING_RIGHT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.dto.TrialAnalysisReportableResultEntry;
import org.cheetahplatform.web.eyetracking.analysis.AbstractTrialAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingStep;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialDetectionNotification;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class MissingTrialAnalyzer extends AbstractTrialAnalyzer {

	public MissingTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType type,
			DataProcessingStep dataProcessingStep) {
		super(config, processing, type, dataProcessingStep);
	}

	@Override
	public Map<String, List<ReportableResultEntry>> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile)
			throws Exception {
		Map<String, List<ReportableResultEntry>> results = new HashMap<>();
		PupillometryFileColumn missingColumn = pupillometryFile.getHeader().getColumn(MISSING_COLUMN);
		if (missingColumn == null) {
			trialEvaluation.setNotifications(Arrays.asList(new TrialDetectionNotification(
					"The input file does not contain a column named " + MISSING_COLUMN
							+ ". This is most likely since no pupil substitution was specified in the the clean step.",
					TrialDetectionNotification.TYPE_ERROR)));
			return results;
		}

		List<Trial> trials = trialEvaluation.getTrials();
		for (Trial trial : trials) {
			double missingCount = 0;
			for (PupillometryFileLine line : trial.getLines()) {
				String missing = line.get(missingColumn);
				boolean isMissing = missing != null
						&& (missing.equals(MISSING_LEFT) || missing.equals(MISSING_RIGHT) || missing.equals(MISSING_BOTH));
				if (isMissing) {
					missingCount++;
				}
			}

			double result = missingCount;
			if (analyzeStep.equals(AnalyzeStepType.MISSING_PERCENT)) {
				result = missingCount / trial.getLines().size();
			}
			TrialAnalysisReportableResultEntry entry = new TrialAnalysisReportableResultEntry(trial.getTrialNumber(),
					String.valueOf(result));
			addResult(results, analyzeStep.getId(), entry);
		}

		return results;
	}
}
