package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.PhaseLabel;
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.analysis.steps.StandardError;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class LabeledPhaseAverageLoadForTsvFileWorkItem extends AbstractCheetahWorkItem {

	private String leftPupilColumn;
	private String rightPupilColumn;
	private CheetahWorkItemGuard guard;
	private List<PhaseLabel> labelList;
	private String labelColumn;
	private String baseline;
	private BaselineValues baselineValues;

	public LabeledPhaseAverageLoadForTsvFileWorkItem(Long fileId, String leftPupilColumn, String rightPupilColumn,
			List<PhaseLabel> labelList, String labelColumn, String baseline, long userId, CheetahWorkItemGuard guard) {
		super(userId, fileId, "Calculating Average Load for .tsv file");
		this.labelColumn = labelColumn;
		this.leftPupilColumn = leftPupilColumn;
		this.rightPupilColumn = rightPupilColumn;
		this.labelList = labelList;
		this.guard = guard;
		this.baseline = baseline;
		this.baselineValues = null;
	}

	private List<TimeFrame> baselineCaclulation(List<TimeFrame> frames) {
		TimeFrame tmp = null;
		UnivariateStatistic method = new Mean();
		for (int i = 0; i < frames.size(); i++) {
			if (frames.get(i).getLabel().equals(baseline)) {
				System.out.println("Gefunden " + frames.get(i).getLabel());
				tmp = frames.get(i);
				frames.remove(i);
				break;
			}
		}
		double[] lefPupilArray = tmp.getLefPupilArray();
		double[] rightPupilArray = tmp.getRightPupilArray();
		Double leftMean = null;
		Double rightMean = null;

		if (lefPupilArray.length > 0) {
			leftMean = method.evaluate(lefPupilArray);
		}
		if (rightPupilArray.length > 0) {
			rightMean = method.evaluate(rightPupilArray);
		}
		if (leftMean == null || rightMean == null) {
			try {
				String errorMessage = "Baseline \"" + baseline + "\" cannot be calculated";
				logErrorNotification(errorMessage);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
		baselineValues = new BaselineValues(leftMean, rightMean);
		return frames;
	}

	private ReportableResult calculate(ReportableResult result, List<TimeFrame> frames, boolean accumulate) {
		String prefix = "";

		if (baseline != null) {
			frames = baselineCaclulation(frames);
			if (frames == null) {
				return null;
			}
			prefix = "baseline_" + baseline + "_";
		}

		PhaseAverageLoadForTsvFileWorkItemCalculation calculateMeasure = new PhaseAverageLoadForTsvFileWorkItemCalculation(frames, result,
				baselineValues);
		calculateMeasure.adFileId(fileId, "FileId");
		calculateMeasure.calculate(new Mean(), prefix + "average");
		calculateMeasure.calculate(new Median(), prefix + "median");
		calculateMeasure.calculate(new Min(), prefix + "min");
		calculateMeasure.calculate(new Max(), prefix + "max");
		calculateMeasure.calculate(new StandardDeviation(), prefix + "standardDeviatiom");
		calculateMeasure.calculate(new StandardError(), prefix + "standardError");
		return calculateMeasure.getReportableResult();
	}

	@Override
	public void cancel() {
		guard.canceled(this);
		super.cancel();
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		File userFile = userFileDao.getUserFile(userFileDao.getPath(fileId));
		PupillometryFile pupillometryFile = new PupillometryFile(userFile, PupillometryFile.SEPARATOR_TABULATOR, true, ".");
		PupillometryFileColumn pupilLeft = pupillometryFile.getHeader().getColumn(leftPupilColumn);
		PupillometryFileColumn label = pupillometryFile.getHeader().getColumn(labelColumn);
		PupillometryFileColumn pupilRigt = pupillometryFile.getHeader().getColumn(rightPupilColumn);

		List<TimeFrame> frames = new ArrayList<>();
		for (PhaseLabel pl : labelList) {
			frames.add(new TimeFrame(pl.getLabel()));
		}

		LinkedList<PupillometryFileLine> content = pupillometryFile.getContent();
		for (PupillometryFileLine pupillometryFileLine : content) {
			String lineLabel = pupillometryFileLine.get(label);
			for (TimeFrame frame : frames) {
				if (frame.getLabel().equals(lineLabel)) {
					if (!pupillometryFileLine.isEmpty(pupilLeft)) {
						frame.addLeft(pupillometryFileLine.getDouble(pupilLeft));
					}
					if (!pupillometryFileLine.isEmpty(pupilRigt)) {
						frame.addRight(pupillometryFileLine.getDouble(pupilRigt));
					}
				}
			}

		}

		String fileName = userFileDao.getFile(fileId).getFilename();
		String[] splitted = splitFileName(fileName);

		ReportableResult reportableResult = new ReportableResult(splitted[0]);
		// DecimalFormat format = new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(
		// Locale.GERMAN));reportableResult.addResult("source_file_name",new ReportableResultEntry(fileName));reportableResult=

		reportableResult = calculate(reportableResult, frames, true);
		if (reportableResult == null) {
			guard.canceled(this);
		} else {
			guard.reportResult(reportableResult);
		}

	}

}
