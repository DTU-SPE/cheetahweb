package org.cheetahplatform.web.dto;

import java.util.List;

public class SequencesOfPupillometryRequestLabeled {
	private List<Long> fileIds;
	private String leftPupilColumn;
	private String rightPupilColumn;
	private String labelColumn;
	private String baseline;
	private List<PhaseLabel> labelList;

	public SequencesOfPupillometryRequestLabeled() {
		super();
	}

	public SequencesOfPupillometryRequestLabeled(List<Long> fileIds, String leftPupilColumn, String rightPupilColumn, String labelColumn,
			String baseline, List<PhaseLabel> labelList) {
		super();
		this.fileIds = fileIds;
		this.leftPupilColumn = leftPupilColumn;
		this.rightPupilColumn = rightPupilColumn;
		this.labelColumn = labelColumn;
		this.baseline = baseline;
		this.labelList = labelList;
	}

	public String getBaseline() {
		return baseline;
	}

	public List<Long> getFileIds() {
		return fileIds;
	}

	public String getLabelColumn() {
		return labelColumn;
	}

	public List<PhaseLabel> getLabelList() {
		return labelList;
	}

	public String getLeftPupilColumn() {
		return leftPupilColumn;
	}

	public String getRightPupilColumn() {
		return rightPupilColumn;
	}

	public void setBaseline(String baseline) {
		this.baseline = baseline;
	}

	public void setFileIds(List<Long> fileIds) {
		this.fileIds = fileIds;
	}

	public void setLabelColumn(String labelColumn) {
		this.labelColumn = labelColumn;
	}

	public void setLabelList(List<PhaseLabel> labelList) {
		this.labelList = labelList;
	}

	public void setLeftPupilColumn(String leftPupilColumn) {
		this.leftPupilColumn = leftPupilColumn;
	}

	public void setRightPupilColumn(String rightPupilColumn) {
		this.rightPupilColumn = rightPupilColumn;
	}

}