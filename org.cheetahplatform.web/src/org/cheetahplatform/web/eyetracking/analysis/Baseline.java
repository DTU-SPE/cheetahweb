package org.cheetahplatform.web.eyetracking.analysis;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileUtils;

public class Baseline extends AbstractPupillometryFileSection {
	private String type;

	public Baseline(String type) {
		this.type = type;
	}

	/**
	 * Calculates the baseline's mean for the given column.
	 *
	 * @param column
	 * @return
	 */
	public double calculateMean(PupillometryFileColumn column) {
		double[] pupils = PupillometryFileUtils.getPupilValues(lines, column, false);
		return new Mean().evaluate(pupils);
	}

	public long getDuration(PupillometryFileColumn timestampColumn) {
		if (lines == null || lines.isEmpty()) {
			return 0;
		}

		PupillometryFileLine firstLine = lines.get(0);
		PupillometryFileLine lastLine = lines.get(lines.size() - 1);

		long startTimeStamp = firstLine.getLong(timestampColumn);
		long endTimeStamp = lastLine.getLong(timestampColumn);
		return endTimeStamp - startTimeStamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
