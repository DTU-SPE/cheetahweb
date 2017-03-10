package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.Iterator;

import org.cheetahplatform.web.dto.FilterRequest;

/**
 * Linear interpolation of missing values.
 *
 * @author Stefan
 *
 */
public class LinearInterpolationFilter extends AbstractPupillometryFilter {

	public LinearInterpolationFilter(long id) {
		super(id, "Linear Interpolation");
	}

	/**
	 * Linearly interpolate an array of values.
	 *
	 * @param values
	 *            the values to be interpolated
	 *
	 * @return the amount of replaced values
	 */
	private int interpolate(double[] values) {
		int replaced = 0;

		for (int i = 0; i < values.length; i++) {
			double value = values[i];
			if (value == 0.0) {
				double start = 0;
				if (i != 0) {
					start = values[i - 1];
				}

				double end = 0;
				int endIndex = 0;
				for (int j = i; j < values.length; j++) {
					if (values[j] != 0.0) {
						end = values[j];
						endIndex = j;
						break;
					}
				}

				double difference = end - start;
				double stepSize = difference / (endIndex - i);
				double current = start;
				for (int j = i; j < endIndex; j++) {
					values[j] = current;
					current += stepSize;
					replaced++;
				}
			}
		}

		return replaced;
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file, long fileId) throws Exception {
		PupillometryFileColumn leftPupil = file.getHeader().getColumn(request.getLeftPupilColumn());
		PupillometryFileColumn rightPupil = file.getHeader().getColumn(request.getRightPupilColumn());
		double[] leftValues = PupillometryFileUtils.getPupilValues(file, leftPupil, true);
		double[] rightValues = PupillometryFileUtils.getPupilValues(file, rightPupil, true);
		int replaced = interpolate(leftValues);
		replaced += interpolate(rightValues);

		Iterator<PupillometryFileLine> iterator = file.getContent().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			PupillometryFileLine line = iterator.next();
			line.setValue(leftPupil, leftValues[index]);
			line.setValue(rightPupil, rightValues[index]);
			index++;
		}

		return "Interpolated " + replaced + " values";
	}

}
