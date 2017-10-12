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
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.TimeSlot;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.analysis.steps.StandardError;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class PhaseAverageLoadForTsvFileWorkItem extends AbstractCheetahWorkItem {

	private String leftPupilColumn;
	private String rightPupilColumn;
	private CheetahWorkItemGuard guard;
	private List<TimeSlot> timeSlots;
	private String timeStampColumn;
	private String baseline;
	private BaselineValues baselineValues;

	public PhaseAverageLoadForTsvFileWorkItem(long fileId, String leftPupilColumn, String rightPupilColumn, List<TimeSlot> timeSlots,
			String timeStampsColumn, String baseline, long userId, CheetahWorkItemGuard guard) {
		super(userId, fileId, "Calculating Average Load for .tsv file");
		this.leftPupilColumn = leftPupilColumn;
		this.rightPupilColumn = rightPupilColumn;
		this.timeStampColumn = timeStampsColumn;
		this.guard = guard;
		this.timeSlots = timeSlots;
		this.baseline = baseline;
		this.baselineValues = null;
	}

	private List<TimeFrame> baselineCaclulation(List<TimeFrame> frames) {
		TimeFrame tmp = null;
		UnivariateStatistic method = new Mean();
		for (int i = 0; i < frames.size(); i++) {
			if (frames.get(i).getLabel().equals(baseline)) {
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

		if (accumulate == true && frames.size() > 1) {
			List<TimeFrame> framesTmp = new ArrayList<>();
			while (frames.size() > 0) {
				LinkedList<Double> pupilLeft = new LinkedList<>();
				LinkedList<Double> pupilRight = new LinkedList<>();
				TimeFrame tmp = frames.get(0);
				pupilLeft.addAll(tmp.getPupilLeft());
				pupilRight.addAll(tmp.getPupilRight());
				for (int i = frames.size() - 1; i > 0; i--) {
					if (frames.get(i).getLabel().equals(tmp.getLabel())) {
						TimeFrame tmp1 = frames.get(i);
						pupilLeft.addAll(tmp1.getPupilLeft());
						pupilRight.addAll(tmp1.getPupilRight());
						frames.remove(i);
					}
				}
				framesTmp.add(new TimeFrame(tmp.getLabel(), pupilLeft, pupilRight));
				frames.remove(0);
			}
			frames = framesTmp;

		}

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
		PupillometryFileColumn time = pupillometryFile.getHeader().getColumn(timeStampColumn);
		PupillometryFileColumn pupilRigt = pupillometryFile.getHeader().getColumn(rightPupilColumn);

		List<TimeFrame> frames = new ArrayList<>();
		for (TimeSlot ts : timeSlots) {
			frames.add(new TimeFrame(ts.getStart(), ts.getEnd(), ts.getSlotLabel()));
		}

		LinkedList<PupillometryFileLine> content = pupillometryFile.getContent();
		for (PupillometryFileLine pupillometryFileLine : content) {
			long timestamp = pupillometryFileLine.getLong(time);
			for (TimeFrame frame : frames) {
				if (frame.matchesTimeFrame(timestamp)) {
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
