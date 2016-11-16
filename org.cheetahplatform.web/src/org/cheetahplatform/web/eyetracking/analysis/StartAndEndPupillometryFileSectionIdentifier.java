package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class StartAndEndPupillometryFileSectionIdentifier extends StartPupillometryFileSectionIdentifier {
	private String end;

	public StartAndEndPupillometryFileSectionIdentifier(String start, String end) {
		super(start);
		this.end = end;
	}

	@Override
	public boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn column) {
		return matches(line, column, end);
	}
}