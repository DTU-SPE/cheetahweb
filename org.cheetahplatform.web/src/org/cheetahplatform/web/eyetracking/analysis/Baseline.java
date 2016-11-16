package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class Baseline extends AbstractPupillometryFileSection {
	private String type;

	public Baseline(String type) {
		this.type = type;
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
