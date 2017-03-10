package org.cheetahplatform.web.eyetracking.cleaning;

import org.cheetahplatform.web.dto.FilterRequest;

/**
 * Filter that copies data of left to right pupil (and vice versa) in case one value is missing.
 *
 * @author Stefan
 *
 */
public class SubstitutePupilFilter extends AbstractPupillometryFilter {

	public static final String MISSING_COLUMN = "MissingValue";
	public static final String MISSING_LEFT = "left";
	public static final String MISSING_RIGHT = "right";
	public static final String MISSING_BOTH = "both";

	public SubstitutePupilFilter(long id) {
		super(id, "Pupil Substitution");
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file, long fileId) throws Exception {
		PupillometryFileColumn leftPupil = file.getHeader().getColumn(request.getLeftPupilColumn());
		PupillometryFileColumn rightPupil = file.getHeader().getColumn(request.getRightPupilColumn());

		PupillometryFileColumn missingColumn = null;
		if (file.hasColumn(MISSING_COLUMN)) {
			missingColumn = file.getHeader().getColumn(MISSING_COLUMN);
		} else {
			missingColumn = file.appendColumn(MISSING_COLUMN);
		}

		int replaced = 0;
		for (PupillometryFileLine line : file.getContent()) {
			String left = line.get(leftPupil);
			String right = line.get(rightPupil);
			boolean leftMissing = left.trim().isEmpty();
			boolean rightMissing = right.trim().isEmpty();

			if (leftMissing && !rightMissing) {
				line.setValue(leftPupil, right);
				replaced++;
				line.setValue(missingColumn, MISSING_LEFT);
			} else if (!leftMissing && rightMissing) {
				line.setValue(rightPupil, left);
				replaced++;
				line.setValue(missingColumn, MISSING_RIGHT);
			} else if (leftMissing && rightMissing) {
				line.setValue(missingColumn, MISSING_BOTH);
			}
		}

		return "Substituted " + replaced + " values";
	}

}
