package org.cheetahplatform.web.eyetracking;

public class EyeTrackingEntry {
	private long timestamp;

	private double leftPupil;

	private double rightPupil;

	private double average;

	public EyeTrackingEntry(long timestamp, double leftPupil, double rightPupil) {
		this.timestamp = timestamp;
		this.leftPupil = leftPupil;
		this.rightPupil = rightPupil;

		if (leftPupil > 0 && rightPupil > 0) {
			this.average = (leftPupil + rightPupil) / 2;
		} else {
			if (leftPupil > 0) {
				this.average = leftPupil;
			} else {
				this.average = rightPupil;
			}
		}
	}

	public double getAverage() {
		return average;
	}

	public double getLeftPupil() {
		return leftPupil;
	}

	public double getRightPupil() {
		return rightPupil;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public void setLeftPupil(double leftPupil) {
		this.leftPupil = leftPupil;
	}

	public void setRightPupil(double rightPupil) {
		this.rightPupil = rightPupil;
	}

}
