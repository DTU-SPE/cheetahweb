package org.cheetahplatform.web.dto;

import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.eyetracking.EyeTrackingEntry;

public class PupillometryDto {
	private String label;
	private long sessionStartTimestamp;
	private List<EyeTrackingEntry> entries;
	private List<EyeTrackingEntry> slidingWindow;
	private Map<Integer, Double> percentiles;

	private double min;

	private double max;
	private long sessionEndTimeStamp;
	private long id;

	public PupillometryDto(long sessionStartTimestamp, long sessionEndTimeStamp, List<EyeTrackingEntry> entries, String label, long id) {
		this.sessionStartTimestamp = sessionStartTimestamp;
		this.sessionEndTimeStamp = sessionEndTimeStamp;
		this.entries = entries;
		this.label = label;
		this.id = id;
	}

	public List<EyeTrackingEntry> getEntries() {
		return entries;
	}

	public long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public Map<Integer, Double> getPercentiles() {
		return percentiles;
	}

	public long getSessionEndTimeStamp() {
		return sessionEndTimeStamp;
	}

	public long getSessionStartTimestamp() {
		return sessionStartTimestamp;
	}

	public List<EyeTrackingEntry> getSlidingWindow() {
		return slidingWindow;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public void setPercentiles(Map<Integer, Double> percentiles) {
		this.percentiles = percentiles;
	}

	public void setSlidingWindow(List<EyeTrackingEntry> slidingWindow) {
		this.slidingWindow = slidingWindow;
	}

}
