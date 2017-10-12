package org.cheetahplatform.web.eyetracking.analysis;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;

public class PhaseAverageLoadForTsvFileWorkItemCalculation {

	private List<TimeFrame> frames;
	private ReportableResult reportableResult;
	private BaselineValues baseline;

	public PhaseAverageLoadForTsvFileWorkItemCalculation(List<TimeFrame> frames, ReportableResult reportableResult,
			BaselineValues baseline) {
		super();
		this.frames = frames;
		this.reportableResult = reportableResult;
		this.baseline = baseline;

	}

	public void adFileId(Long fileId, String string) {
		reportableResult.addResult(string, new ReportableResultEntry(fileId.toString()));

	}

	public void calculate(UnivariateStatistic method, String measureName) {
		DecimalFormat format = new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.GERMAN));
		for (TimeFrame tf : frames) {
			double[] lefPupilArray = tf.getLefPupilArray();
			double[] rightPupilArray = tf.getRightPupilArray();
			if (baseline != null) {
				for (int i = 0; i < lefPupilArray.length; i++) {
					lefPupilArray[i] = lefPupilArray[i] - baseline.getBaselinePupilLeft();
				}
				for (int i = 0; i < rightPupilArray.length; i++) {
					rightPupilArray[i] = rightPupilArray[i] - baseline.getBaselinePupilLeft();
				}
			}
			ReportableResultEntry errorIfNoData = new ReportableResultEntry("No data for this timeslot available");
			if (lefPupilArray.length > 0) {
				double leftMean = method.evaluate(lefPupilArray);
				reportableResult.addResult(measureName + "_pupil_left_" + tf.getLabel(),
						new ReportableResultEntry(format.format(leftMean)));
			} else {
				reportableResult.addResult(measureName + "_pupil_left_" + tf.getLabel(), errorIfNoData);
			}
			if (rightPupilArray.length > 0) {
				double rightMean = method.evaluate(rightPupilArray);
				reportableResult.addResult(measureName + "_pupil_right_" + tf.getLabel(),
						new ReportableResultEntry(format.format(rightMean)));
			} else {
				reportableResult.addResult(measureName + "_pupil_right_" + tf.getLabel(), errorIfNoData);
			}
		}
	}

	public ReportableResult getReportableResult() {
		return reportableResult;
	}

}