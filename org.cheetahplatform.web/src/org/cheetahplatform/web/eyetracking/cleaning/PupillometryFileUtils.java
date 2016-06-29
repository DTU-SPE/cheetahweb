package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PupillometryFileUtils {

	/**
	 * Get the pupil values
	 *
	 * @param file
	 *            the file to be processed
	 * @param column
	 *            the columns to be processed
	 * @param subsituteMissings
	 *            if <code>true</code> missing values are substituted with 0, if <code>false</code> missing values are ignored and not added
	 *            to the result
	 * @return
	 * @throws IOException
	 */
	public static double[] getPupilValues(PupillometryFile file, PupillometryFileColumn column, boolean subsituteMissings)
			throws IOException {
		List<PupillometryFileLine> content = file.getContent();
		List<Double> pupils = new ArrayList<Double>();
		for (PupillometryFileLine line : content) {
			if (line.isEmpty(column)) {
				if (subsituteMissings) {
					pupils.add(0.0);
				}
	
				continue;
			}
	
			pupils.add(line.getDouble(column));
		}
	
		double[] result = new double[pupils.size()];
		for (int i = 0; i < pupils.size(); i++) {
			result[i] = pupils.get(i);
		}
	
		return result;
	}

}
