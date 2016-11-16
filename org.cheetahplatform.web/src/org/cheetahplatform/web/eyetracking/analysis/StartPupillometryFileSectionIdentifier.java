package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class StartPupillometryFileSectionIdentifier extends AbstractPupillometryFileSectionIdentifier {
	protected String start;

	public StartPupillometryFileSectionIdentifier(String start) {
		this.start = start;
	}

	@Override
	public boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn column) {
		// run until the end
		return false;
	}

	@Override
	public boolean isStart(IPupillometryFileLine line, PupillometryFileColumn column) {
		return matches(line, column, start);
	}

}
