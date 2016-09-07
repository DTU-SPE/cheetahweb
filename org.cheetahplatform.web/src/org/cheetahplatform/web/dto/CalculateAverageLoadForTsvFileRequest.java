package org.cheetahplatform.web.dto;

import java.util.List;

public class CalculateAverageLoadForTsvFileRequest {
	private List<Long> fileIds;
	private String leftPupilColumn;
	private String rightPupilColumn;

	public List<Long> getFileIds() {
		return fileIds;
	}

	public String getLeftPupilColumn() {
		return leftPupilColumn;
	}

	public String getRightPupilColumn() {
		return rightPupilColumn;
	}

	public void setFileIds(List<Long> fileIds) {
		this.fileIds = fileIds;
	}

	public void setLeftPupilColumn(String leftPupilColumn) {
		this.leftPupilColumn = leftPupilColumn;
	}

	public void setRightPupilColumn(String rightPupilColumn) {
		this.rightPupilColumn = rightPupilColumn;
	}
}
