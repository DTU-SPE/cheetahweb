package org.cheetahplatform.web.eyetracking.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileUtils;

public abstract class AbstractPupilTrialAnalyzer extends AbstractTrialAnalyzer {

	public static final long NO_TIMESTAMP_SET = -1;
	private long startTime;
	private long endTime;

	public AbstractPupilTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, AnalyzeStepType analyzeStep, long startTime,
			long endTime) {
		super(config, processing, analyzeStep);

		this.startTime = startTime;
		this.endTime = endTime;
	}

	protected void addAveragePupilSizeToResults(Trial trial, Map<String, String> results, double left, double right) {
		if (left != 0 && right != 0) {
			double average = (left + right) / 2;
			addResult(results, trial, average, PUPIL_AVERAGE);
		}
	}

	@Override
	public Map<String, String> analyze(TrialEvaluation trialEvaluation, PupillometryFile pupillometryFile) throws Exception {
		PupillometryFileColumn leftPupilColumn = pupillometryFile.getHeader().getColumn(dataProcessing.getLeftPupilColumn());
		PupillometryFileColumn rightPupilColumn = pupillometryFile.getHeader().getColumn(dataProcessing.getRightPupilColumn());

		Map<String, String> results = new HashMap<>();
		List<Trial> trials = trialEvaluation.getTrials();
		for (Trial trial : trials) {
			analyzeTrial(pupillometryFile, trial, results, leftPupilColumn, rightPupilColumn);
		}

		return results;
	}

	protected abstract void analyzeTrial(PupillometryFile pupillometryFile, Trial trial, Map<String, String> results,
			PupillometryFileColumn leftPupilColumn, PupillometryFileColumn rightPupilColumn);

	protected double calculateMean(List<PupillometryFileLine> lines, PupillometryFileColumn column) {
		double[] pupils = PupillometryFileUtils.getPupilValues(lines, column, false);
		return new Mean().evaluate(pupils);
	}

	protected double[] getPupilValues(PupillometryFile file, PupillometryFileColumn column, List<PupillometryFileLine> lines) {
		boolean hasEndTime = endTime != NO_TIMESTAMP_SET;
		boolean hasStartTime = startTime != NO_TIMESTAMP_SET;
		// no explicit durations set, analyze the entire stimulus
		if (!hasStartTime && !hasEndTime) {
			return PupillometryFileUtils.getPupilValues(lines, column, false);
		}

		List<PupillometryFileLine> linesForDuration = new ArrayList<>();
		try {
			PupillometryFileColumn timestampColumn = file.getHeader().getColumn(dataProcessing.getTimestampColumn());
			long startTimestamp = lines.get(0).getLong(timestampColumn);
			long startTime = startTimestamp;
			if (hasStartTime) {
				startTime = startTimestamp + this.startTime * 1000;
			}
			long endTime = startTimestamp + this.endTime * 1000;

			for (PupillometryFileLine line : lines) {
				long timestamp = line.getLong(timestampColumn);
				if (hasStartTime && timestamp < startTime) {
					continue;
				}
				if (hasEndTime && timestamp > endTime) {
					break;
				}
				linesForDuration.add(line);
			}

			// see if we need to collect values outside the trial as well
			long lastTimestamp = lines.get(lines.size() - 1).getLong(timestampColumn);
			if (hasEndTime && lastTimestamp < endTime) {
				Iterator<PupillometryFileLine> iterator = file.getIteratorStartingAt(lastTimestamp, timestampColumn);
				while (iterator.hasNext()) {
					PupillometryFileLine currentLine = iterator.next();
					long currentTimestamp = currentLine.getLong(timestampColumn);
					if (currentTimestamp > endTime) {
						break;
					}

					linesForDuration.add(currentLine);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}

		return PupillometryFileUtils.getPupilValues(linesForDuration, column, false);
	}

}