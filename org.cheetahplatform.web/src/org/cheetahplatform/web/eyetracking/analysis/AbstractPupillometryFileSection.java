package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class AbstractPupillometryFileSection {

	protected List<PupillometryFileLine> lines;

	public AbstractPupillometryFileSection() {
		super();
		lines = new ArrayList<>();
	}

	public void addLine(PupillometryFileLine line) {
		lines.add(line);
	}

	public List<PupillometryFileLine> getLines() {
		return Collections.unmodifiableList(lines);
	}
}