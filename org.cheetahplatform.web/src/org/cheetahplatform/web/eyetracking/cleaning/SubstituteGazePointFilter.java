package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.List;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dto.FilterRequest;

public class SubstituteGazePointFilter extends AbstractPupillometryFilter {

	private static final String MISSING_COLUMN = "GazePointMissing";
	private static final String MISSING_LEFT = "left";
	private static final String MISSING_RIGHT = "right";
	private static final String MISSING_BOTH = "both";

	public SubstituteGazePointFilter(long id) {
		super(id, "Gaze Point Substitution");
	}

	@Override
	protected List<PupillometryParameter> getParameters() {
		List<PupillometryParameter> parameters = super.getParameters();
		parameters.add(new PupillometryParameter(CheetahWebConstants.LEFT_PUPIL_GAZE_X_PX, "Gazepoint X Left Pupil in Pixel",
				"XGazePosLeftEye", true));
		parameters.add(new PupillometryParameter(CheetahWebConstants.LEFT_PUPIL_GAZE_Y_PX, "Gazepoint Y Left Pupil in Pixel",
				"YGazePosLeftEye", true));
		parameters.add(new PupillometryParameter(CheetahWebConstants.RIGHT_PUPIL_GAZE_X_PX, "Gazepoint X Right Pupil in Pixel",
				"XGazePosRightEye", true));
		parameters.add(new PupillometryParameter(CheetahWebConstants.RIGHT_PUPIL_GAZE_Y_PX, "Gazepoint Y Right Pupil in Pixel",
				"YGazePosRightEye", true));
		return parameters;
	}

	private boolean isMissing(String x, String y) {
		return x.trim().isEmpty() || y.trim().isEmpty();
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file, long fileId) throws Exception {
		PupillometryFileColumn leftPupilGazeX = file.getHeader().getColumn(request.getParameter(CheetahWebConstants.LEFT_PUPIL_GAZE_X_PX));
		PupillometryFileColumn leftPupilGazeY = file.getHeader().getColumn(request.getParameter(CheetahWebConstants.LEFT_PUPIL_GAZE_Y_PX));
		PupillometryFileColumn rightPupilGazeX = file.getHeader()
				.getColumn(request.getParameter(CheetahWebConstants.RIGHT_PUPIL_GAZE_X_PX));
		PupillometryFileColumn rightPupilGazeY = file.getHeader()
				.getColumn(request.getParameter(CheetahWebConstants.RIGHT_PUPIL_GAZE_Y_PX));

		PupillometryFileColumn missingColumn = null;
		if (file.hasColumn(MISSING_COLUMN)) {
			missingColumn = file.getHeader().getColumn(MISSING_COLUMN);
		} else {
			missingColumn = file.appendColumn(MISSING_COLUMN);
		}

		long substituted = 0;

		for (PupillometryFileLine line : file.getContent()) {
			String leftX = line.get(leftPupilGazeX);
			String leftY = line.get(leftPupilGazeY);
			String rightX = line.get(rightPupilGazeX);
			String rightY = line.get(rightPupilGazeY);
			boolean leftMissing = isMissing(leftX, leftY);
			boolean rightMissing = isMissing(rightX, rightX);

			if (leftMissing && !rightMissing) {
				line.setValue(leftPupilGazeX, rightX);
				line.setValue(leftPupilGazeY, rightY);
				substituted++;
				line.setValue(missingColumn, MISSING_LEFT);
			} else if (!leftMissing && rightMissing) {
				line.setValue(rightPupilGazeX, leftX);
				line.setValue(rightPupilGazeY, leftY);
				substituted++;
				line.setValue(missingColumn, MISSING_RIGHT);
			} else if (leftMissing && rightMissing) {
				line.setValue(missingColumn, MISSING_BOTH);
			}
		}

		return "Substituted " + substituted + " gaze point values";
	}
}
