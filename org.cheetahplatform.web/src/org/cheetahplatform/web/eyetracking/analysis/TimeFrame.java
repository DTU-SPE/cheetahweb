package org.cheetahplatform.web.eyetracking.analysis;

import java.util.LinkedList;

public class TimeFrame {
	private long start;
	private long end;
	private String label;
	private LinkedList<Double> pupilLeft;
	private LinkedList<Double> pupilRight;

	public TimeFrame(long start, long end, String label) {
		super();
		this.start = start;
		this.end = end;
		this.label = label;

		this.pupilLeft = new LinkedList<>();
		this.pupilRight = new LinkedList<>();
	}

	public TimeFrame(String label) {
		super();
		this.label = label;

		this.pupilLeft = new LinkedList<>();
		this.pupilRight = new LinkedList<>();
	}

	public TimeFrame(String label, LinkedList<Double> pupilLeft, LinkedList<Double> pupilRight) {
		super();
		this.label = label;
		this.pupilLeft = pupilLeft;
		this.pupilRight = pupilRight;
	}

	public void addLeft(double value) {
		pupilLeft.add(value);
	}

	public void addRight(double value) {
		pupilRight.add(value);
	}

	public String getLabel() {
		return label;
	}

	public double[] getLefPupilArray() {
		double[] returnArray = new double[pupilLeft.size()];
		for (int i = 0; i < returnArray.length; i++) {
			returnArray[i] = pupilLeft.get(i);
		}
		return returnArray;
	}

	public LinkedList<Double> getPupilLeft() {
		return pupilLeft;
	}

	public LinkedList<Double> getPupilRight() {
		return pupilRight;
	}

	public double[] getRightPupilArray() {
		double[] returnArray = new double[pupilRight.size()];
		for (int i = 0; i < returnArray.length; i++) {
			returnArray[i] = pupilRight.get(i);
		}
		return returnArray;
	}

	public boolean matchesTimeFrame(long timestamp) {
		return start <= timestamp && end >= timestamp;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
