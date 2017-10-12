package org.cheetahplatform.web.dto;

import java.util.List;

public class SequencesOfPupillometryRequest {
	private List<Long> fileIds;
	private String leftPupilColumn;
	private String rightPupilColumn;
	private String timeStampsColumn;
	private String baseline;
	private List<TimeSlot> timeSlots;

	public SequencesOfPupillometryRequest() {
		super();
	}

	public SequencesOfPupillometryRequest(List<Long> fileIds, String leftPupilColumn, String rightPupilColumn, String timeStampsColumn,
			String baseline, List<TimeSlot> timeSlots) {
		super();
		this.fileIds = fileIds;
		this.leftPupilColumn = leftPupilColumn;
		this.rightPupilColumn = rightPupilColumn;
		this.timeStampsColumn = timeStampsColumn;
		this.baseline = baseline;
		this.timeSlots = timeSlots;
	}

	public String getBaseline() {
		return baseline;
	}

	public List<Long> getFileIds() {
		return fileIds;
	}

	public String getLeftPupilColumn() {
		return leftPupilColumn;
	}

	public String getRightPupilColumn() {
		return rightPupilColumn;
	}

	public List<TimeSlot> getTimeSlots() {
		return timeSlots;
	}

	public String getTimeStampsColumn() {
		return timeStampsColumn;
	}

	public void setBaseline(String baseline) {
		this.baseline = baseline;
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

	public void setTimeSlots(List<TimeSlot> timeSlots) {
		this.timeSlots = timeSlots;
	}

	public void setTimeStampsColumn(String timeStampsColumn) {
		this.timeStampsColumn = timeStampsColumn;
	}

	@Override
	public String toString() {
		String ts = "";
		for (TimeSlot t : timeSlots) {
			ts = ts + t.getSlotLabel() + " " + t.getStart() + " " + t.getEnd() + " ";
		}

		return "SequencesOfPupillometryRequest [fileIds=" + fileIds + ", leftPupilColumn=" + leftPupilColumn + ", rightPupilColumn="
				+ rightPupilColumn + ", timeSlots=" + ts + "]";
	}
}
