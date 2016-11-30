package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dto.FilterRequest;

public class BlinkDetectionFilter extends AbstractPupillometryFilter {
	public static final String BLINK_COLUMN = "BlinkPedrotti";
	public static final String BLINK_LEFT = "left";
	public static final String BLINK_RIGHT = "right";
	public static final String BLINK_BOTH = "both";

	private static final String BLINK_LEFT_MARKING = "BLINK_LEFT";
	private static final String BLINK_RIGHT_MARKING = "BLINK_RIGHT";
	private static final String DELETED_LEFT_MARKING = "DELETED_LEFT";
	private static final String ARTIFACT_LEFT_MARKING = "ARTIFACT_LEFT";
	private static final String DELETED_RIGHT_MARKING = "DELETED_RIGHT";
	private static final String ARTIFACT_RIGHT_MARKING = "ARTIFACT_RIGHT";

	public BlinkDetectionFilter(long id) {
		super(id, "Blink Detection, Pedrotti (2011)");
	}

	private boolean alignData(PupillometryFile file, PupillometryFileColumn pupilColumn, PupillometryFileColumn gazeXColumn,
			PupillometryFileColumn gazeYColumn) throws IOException {
		LinkedList<PupillometryFileLine> content = file.getContent();
		ListIterator<PupillometryFileLine> iterator = content.listIterator();

		boolean changed = false;
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			if (current.isEmpty(pupilColumn)) {
				continue;
			}

			if (iterator.hasPrevious()) {
				PupillometryFileLine previous = iterator.previous();
				if (previous.isEmpty(pupilColumn)) {
					if (isInvalidGazePoint(current, gazeXColumn, gazeYColumn)) {
						current.deleteValue(pupilColumn);
						changed = true;
					}
				}

				iterator.next();
			}

			if (iterator.hasNext()) {
				PupillometryFileLine next = iterator.next();
				if (next.isEmpty(gazeYColumn)) {
					if (isInvalidGazePoint(current, gazeXColumn, gazeYColumn)) {
						current.deleteValue(pupilColumn);
						changed = true;
					}
				}
				iterator.previous();
			}
		}

		return changed;
	}

	private void cleanPupil(PupillometryFile file, PupillometryFileColumn leftPupilColumn, PupillometryFileColumn leftPupilGazeXColumn,
			PupillometryFileColumn leftPupilGazeYColumn, String deletedMarking, String artifactMarking) throws IOException {

		while (true) {
			boolean changed = false;
			changed = markArtifacts(file, leftPupilColumn, artifactMarking) || changed;
			changed = removeArtifacts(file, leftPupilColumn, artifactMarking, deletedMarking) || changed;
			changed = alignData(file, leftPupilColumn, leftPupilGazeXColumn, leftPupilGazeYColumn) || changed;
			changed = validateData(file, leftPupilColumn, leftPupilGazeXColumn, leftPupilGazeYColumn) || changed;

			if (!changed) {
				break;
			}
		}
	}

	/**
	 * Copy the markings to a new column for persisting them.
	 *
	 * @param file
	 * @throws IOException
	 */
	private void copyMarks(PupillometryFile file) throws IOException {
		PupillometryFileColumn blinkColumn = file.appendColumn(BLINK_COLUMN);

		for (PupillometryFileLine line : file.getContent()) {
			boolean isLeftBlink = line.isMarked(BLINK_LEFT_MARKING);
			boolean isRightBlink = line.isMarked(BLINK_RIGHT_MARKING);

			if (isLeftBlink && !isRightBlink) {
				line.setValue(blinkColumn, BLINK_LEFT);
			} else if (!isLeftBlink && isRightBlink) {
				line.setValue(blinkColumn, BLINK_RIGHT);
			} else if (isLeftBlink && isRightBlink) {
				line.setValue(blinkColumn, BLINK_BOTH);
			}
		}
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
		parameters.add(new PupillometryParameter(CheetahWebConstants.BLINK_DETECTION_TIME_THRESHOLD, "Time before and after blink [ms]",
				"100", false));
		return parameters;
	}

	private boolean isInvalidGazePoint(PupillometryFileLine line, PupillometryFileColumn gazeXColumn, PupillometryFileColumn gazeYColumn) {
		if (line.isEmpty(gazeYColumn) || line.isEmpty(gazeXColumn)) {
			return true;
		}

		double relativeGazePointX = 0;
		double relativeGazePointY = 0;

		// gaze points may be relative (0 to 1) or absolute - handle both cases
		try {
			double gazePointX = line.getInteger(gazeXColumn);
			double gazePointY = line.getInteger(gazeYColumn);
			relativeGazePointX = gazePointX / CheetahWebConstants.DISPLAY_SIZE_X;
			relativeGazePointY = gazePointY / CheetahWebConstants.DISPLAY_SIZE_Y;
		} catch (NumberFormatException e) {
			relativeGazePointX = line.getDouble(gazeXColumn);
			relativeGazePointY = line.getDouble(gazeYColumn);
		}

		return relativeGazePointX < 0 || relativeGazePointX > 1 || relativeGazePointY < 0 || relativeGazePointY > 1;
	}

	private boolean markArtifacts(PupillometryFile file, PupillometryFileColumn column, String artifactMarking) throws IOException {
		double[] pupils = getPupilValues(file, column);
		double standardDeviation = new StandardDeviation().evaluate(pupils);
		double mean = new Mean().evaluate(pupils);
		double upperBound = mean + 3 * standardDeviation;
		double lowerBound = mean - 3 * standardDeviation;

		boolean changes = false;
		List<PupillometryFileLine> lines = file.getContent();
		for (PupillometryFileLine line : lines) {
			if (line.isEmpty(column) || line.isMarked(artifactMarking)) {
				continue;
			}

			Double value = line.getDouble(column);
			if (value < lowerBound || value > upperBound) {
				line.mark(artifactMarking);
				changes = true;
			}
		}

		return changes;
	}

	private boolean removeArtifacts(PupillometryFile file, PupillometryFileColumn leftPupilColumn, String artifactMarking,
			String deletedMarking) throws IOException {
		boolean changed = false;
		LinkedList<PupillometryFileLine> content = file.getContent();
		ListIterator<PupillometryFileLine> iterator = content.listIterator();
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			if (!current.isEmpty(leftPupilColumn)) {
				continue;
			}
			if (iterator.hasPrevious()) {
				PupillometryFileLine previous = iterator.previous();
				if (previous.isEmpty(leftPupilColumn)) {
					if (previous.isMarked(artifactMarking) && (!previous.isMarked(deletedMarking))) {
						previous.deleteValue(leftPupilColumn);
						previous.mark(deletedMarking);
						changed = true;
					}
				}
				iterator.next();
			}

			if (iterator.hasNext()) {
				PupillometryFileLine next = iterator.next();
				if (next.isMarked(artifactMarking) && (!next.isMarked(deletedMarking))) {
					next.deleteValue(leftPupilColumn);
					next.mark(deletedMarking);
					changed = true;
				}
				iterator.previous();
			}
		}
		return changed;
	}

	private int removeBlinks(PupillometryFile file, PupillometryFileColumn leftPupilColumn, PupillometryFileColumn timestampColumn,
			long threshold, String blinkMarking) throws IOException {
		LinkedList<PupillometryFileLine> content = file.getContent();
		ListIterator<PupillometryFileLine> iterator = content.listIterator();
		int blinks = 0;

		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			// some additional info is logged without timestamp
			if (current.isEmpty(timestampColumn)) {
				continue;
			}
			if (current.isMarked(blinkMarking)) {
				continue;
			}

			if (current.isEmpty(leftPupilColumn)) {
				blinks++;
				current.mark(blinkMarking);
				long firstEmtpy = current.getLong(timestampColumn);

				while (iterator.hasPrevious()) {
					PupillometryFileLine previous = iterator.previous();
					if (previous.isEmpty(timestampColumn)) {
						continue;
					}

					if (previous.getLong(timestampColumn) > firstEmtpy - threshold) {
						previous.mark(blinkMarking);
						previous.deleteValue(leftPupilColumn);
					} else {
						break;
					}
				}

				long lastEmpty = firstEmtpy;
				while (iterator.hasNext()) {
					PupillometryFileLine next = iterator.next();
					if (next.isEmpty(timestampColumn)) {
						continue;
					}
					if (next.isEmpty(leftPupilColumn)) {
						lastEmpty = next.getLong(timestampColumn);
					}

					long nextTimestamp = next.getLong(timestampColumn);
					if (nextTimestamp < lastEmpty + threshold) {
						next.mark(blinkMarking);
						next.deleteValue(leftPupilColumn);
					} else {
						break;
					}
				}
			}
		}

		return blinks;
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file) throws Exception {
		PupillometryFileHeader header = file.getHeader();
		PupillometryFileColumn leftPupilColumn = getLeftPupilColumn(request, header);
		PupillometryFileColumn rightPupilColumn = getRightPupilColumn(request, header);
		PupillometryFileColumn leftPupilGazeXColumn = header
				.getColumn(request.getParameters().get(CheetahWebConstants.LEFT_PUPIL_GAZE_X_PX));
		if (leftPupilGazeXColumn == null) {

		}
		PupillometryFileColumn leftPupilGazeYColumn = header
				.getColumn(request.getParameters().get(CheetahWebConstants.LEFT_PUPIL_GAZE_Y_PX));
		PupillometryFileColumn rightPupilGazeXColumn = header
				.getColumn(request.getParameters().get(CheetahWebConstants.RIGHT_PUPIL_GAZE_X_PX));
		PupillometryFileColumn rightPupilGazeYColumn = header
				.getColumn(request.getParameters().get(CheetahWebConstants.RIGHT_PUPIL_GAZE_Y_PX));
		PupillometryFileColumn timestampColumn = header.getColumn(request.getParameters().get(CheetahWebConstants.TIMESTAMP));

		cleanPupil(file, leftPupilColumn, leftPupilGazeXColumn, leftPupilGazeYColumn, DELETED_LEFT_MARKING, ARTIFACT_LEFT_MARKING);
		cleanPupil(file, rightPupilColumn, rightPupilGazeXColumn, rightPupilGazeYColumn, DELETED_RIGHT_MARKING, ARTIFACT_RIGHT_MARKING);

		String parameter = request.getParameter(CheetahWebConstants.BLINK_DETECTION_TIME_THRESHOLD);
		long threshold = Long.parseLong(parameter) * 1000;

		int blinksLeft = removeBlinks(file, leftPupilColumn, timestampColumn, threshold, BLINK_LEFT_MARKING);
		int blinksRight = removeBlinks(file, rightPupilColumn, timestampColumn, threshold, BLINK_RIGHT_MARKING);

		copyMarks(file);
		return "Blinks left: " + blinksLeft + "; Blinks Right: " + blinksRight;
	}

	private boolean validateData(PupillometryFile file, PupillometryFileColumn column, PupillometryFileColumn gazeXColumn,
			PupillometryFileColumn gazeYColumn) throws IOException {
		boolean changed = false;
		LinkedList<PupillometryFileLine> content = file.getContent();
		ListIterator<PupillometryFileLine> iterator = content.listIterator();
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			if (current.isEmpty(column)) {
				continue;
			}

			if (current.isEmpty(gazeXColumn) || current.isEmpty(gazeYColumn)) {
				current.deleteValue(column);
				changed = true;
				continue;
			}

			if (isInvalidGazePoint(current, gazeXColumn, gazeYColumn)) {
				current.deleteValue(column);
				changed = true;
			}
		}

		return changed;
	}
}
