package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public abstract class AbstractPupillopmetryFileDetector {

	public AbstractPupillopmetryFileDetector() {
		super();
	}

	@SuppressWarnings("unchecked")
	protected List<PupillometryFileLine> extractLinesToConsider(PupillometryFileLine line) {
		List<PupillometryFileLine> linesToCheck = new ArrayList<>();
		linesToCheck.add(line);
		List<PupillometryFileLine> collapsedLines = (List<PupillometryFileLine>) line.getMarking(PupillometryFile.COLLAPSED_COLUMNS);
		if (collapsedLines != null) {
			linesToCheck.addAll(collapsedLines);
		}

		return linesToCheck;
	}
}