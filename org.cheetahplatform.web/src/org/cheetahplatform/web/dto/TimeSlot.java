package org.cheetahplatform.web.dto;

import java.util.ArrayList;

public class TimeSlot {
	private Long start;
	private Long end;
	private String slotLabel;

	public TimeSlot() {
	}

	public TimeSlot(Long start, Long end, String slotLabel) {
		super();
		this.start = start;
		this.end = end;
		this.slotLabel = slotLabel;
	}

	public String checkConsistency(ArrayList<TimeSlot> timeSlotList) {
		for (TimeSlot ts : timeSlotList) {
			if (this.start < ts.getEnd() && this.start > ts.getStart()) {
				return "There is a intersection of timeslots";
			}
			if (this.end > ts.getStart() && this.end < ts.getEnd()) {
				return "There is a intersection of timeslots";
			}
			// if (this.slotLabel.equals(ts.getSlotLabel())) {
			// return "There are duplicates in the name";
			// }
		}
		return "";
	}

	public Long getEnd() {
		return end;
	}

	public String getSlotLabel() {
		return slotLabel;
	}

	public Long getStart() {
		return start;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public void setSlotLabel(String slotLabel) {
		this.slotLabel = slotLabel;
	}

	public void setStart(Long start) {
		this.start = start;
	}

}
