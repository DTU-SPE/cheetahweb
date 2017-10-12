package org.cheetahplatform.web.eyetracking.analysis;

public class BaselineValues {

	private double baselinePupilLeft;
	private double baselinePupilRight;

	public BaselineValues(double baselinePupilLeft, double baselinePupilRight) {
		super();
		this.baselinePupilLeft = baselinePupilLeft;
		this.baselinePupilRight = baselinePupilRight;
	}

	public double getBaselinePupilLeft() {
		return baselinePupilLeft;
	}

	public double getBaselinePupilRight() {
		return baselinePupilRight;
	}

	public void setBaselinePupilLeft(double baselinePupilLeft) {
		this.baselinePupilLeft = baselinePupilLeft;
	}

	public void setBaselinePupilRight(double baselinePupilRight) {
		this.baselinePupilRight = baselinePupilRight;
	}

}
