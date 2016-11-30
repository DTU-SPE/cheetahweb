package org.cheetahplatform.web.eyetracking.analysis.steps;

public class AnalyzeConfiguration {
	private String type;
	private long startTime;
	private long endTime;

	public long getEndTime() {
		return endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getType() {
		return type;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setType(String type) {
		this.type = type;
	}

}
