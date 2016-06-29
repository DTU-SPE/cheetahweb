package org.cheetahplatform.web.eyetracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedEyeTrackingData {
	private String label = "no label";
	private long cacheInsertionTimestamp;
	private long id;
	private List<EyeTrackingEntry> entries;
	private Map<Integer, Double> percentiles;
	private Map<Integer, CachedEyeTrackingData> precomputedWindows;
	private double min;
	private double max;
	private boolean cachedValuesComputed;

	public CachedEyeTrackingData() {
		this.entries = new ArrayList<EyeTrackingEntry>();
		this.percentiles = new HashMap<Integer, Double>();
		this.precomputedWindows = new HashMap<Integer, CachedEyeTrackingData>();
		this.cacheInsertionTimestamp = System.currentTimeMillis();
		this.cachedValuesComputed = false;
	}

	public CachedEyeTrackingData(String label, long id) {
		this();
		this.label = label;
		this.id = id;
	}

	public void addEntry(EyeTrackingEntry entry) {
		this.entries.add(entry);
	}

	public boolean areCachedValuesComputed() {
		return cachedValuesComputed;
	}

	public void computeCachedValues() {
		List<Double> averages = new ArrayList<Double>();
		for (EyeTrackingEntry current : entries) {
			double average = (current.getLeftPupil() + current.getRightPupil()) / 2;
			averages.add(average);
		}

		Collections.sort(averages);
		int size = averages.size();
		min = averages.get(0);
		max = averages.get(size - 1);
		percentiles.put(0, min);
		percentiles.put(100, max);

		for (int i = 1; i < 100; i++) {
			int index = (int) ((i / 100.0) * size);
			percentiles.put(i, averages.get(index));
		}

		cachedValuesComputed = true;
	}

	public CachedEyeTrackingData computeSlidingWindow(int duration) {
		CachedEyeTrackingData cachedWindow = precomputedWindows.get(duration);
		if (cachedWindow != null) {
			return cachedWindow;
		}

		if (precomputedWindows.size() > 20) {
			CachedEyeTrackingData oldest = new CachedEyeTrackingData();
			int oldestKey = 0;

			for (Map.Entry<Integer, CachedEyeTrackingData> entry : precomputedWindows.entrySet()) {
				CachedEyeTrackingData current = entry.getValue();

				if (current.isOlderThan(oldest)) {
					oldest = current;
					oldestKey = entry.getKey();
				}
			}

			precomputedWindows.remove(oldestKey);
		}

		cachedWindow = new CachedEyeTrackingData();
		Window window = new Window(duration);
		for (EyeTrackingEntry entry : entries) {
			window.add(entry);
			double average = window.average();
			EyeTrackingEntry median = window.getMedian();
			cachedWindow.addEntry(new EyeTrackingEntry(median.getTimestamp(), average, average));
		}

		precomputedWindows.put(duration, cachedWindow);
		return cachedWindow;
	}

	public long getCacheInsertionTimestamp() {
		return cacheInsertionTimestamp;
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

	public long getSessionStart() {
		return entries.get(0).getTimestamp();
	}

	public boolean isOlderThan(CachedEyeTrackingData oldest) {
		return cacheInsertionTimestamp < oldest.getCacheInsertionTimestamp();
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
