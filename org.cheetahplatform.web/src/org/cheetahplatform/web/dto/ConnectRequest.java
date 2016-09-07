package org.cheetahplatform.web.dto;

import java.util.List;

public class ConnectRequest {
	private String timestampColumn;
	private String leftPupilColumn;
	private String rightPupilColumn;
	private List<Long> files;

	public List<Long> getFiles() {
		return files;
	}

	public String getLeftPupilColumn() {
		return leftPupilColumn;
	}

	public String getRightPupilColumn() {
		return rightPupilColumn;
	}

	public String getTimestampColumn() {
		return timestampColumn;
	}

	public void setFiles(List<Long> files) {
		this.files = files;
	}

	public void setLeftPupilColumn(String leftPupilColumn) {
		this.leftPupilColumn = leftPupilColumn;
	}

	public void setRightPupilColumn(String rightPupilColumn) {
		this.rightPupilColumn = rightPupilColumn;
	}

	public void setTimestampColumn(String timestampColumn) {
		this.timestampColumn = timestampColumn;
	}
}
