package org.cheetahplatform.web.eyetracking;

import java.util.LinkedList;
import java.util.List;

public class Window {
	private List<EyeTrackingEntry> queue;
	private int windowDuration;

	public Window(int windowDuration) {
		this.windowDuration = windowDuration;
		queue = new LinkedList<EyeTrackingEntry>();
	}

	public void add(EyeTrackingEntry value) {
		if (queue.size() > 2) {
			EyeTrackingEntry firstValue = queue.get(0);
			if (value.getTimestamp() - firstValue.getTimestamp() > windowDuration) {
				queue.remove(0);
			}
		}

		queue.add(value);
	}

	public double average() {
		double sum = 0;
		for (EyeTrackingEntry value : queue) {
			sum += value.getAverage();
		}

		return sum / queue.size();
	}

	public double stdDev() {
		double average = average();
		double sum = 0;

		for (EyeTrackingEntry value : queue) {
			sum += Math.abs(average - value.getAverage());
		}

		return sum / queue.size();
	}

	public EyeTrackingEntry getMedian() {
		int size = queue.size();
		return queue.get(size / 2);
	}
}