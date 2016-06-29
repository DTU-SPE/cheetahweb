package org.cheetahplatform.web.dto;

public class PercentileDto {
	private long start;
	private long end;
	private double average;

	public PercentileDto(long start, long end, double average) {
		this.start = start;
		this.end = end;
		this.average = average;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public double getAverage() {
		return average;
	}
	

}
