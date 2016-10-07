package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Set;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class StartPupillometryFileSectionIdentifier implements IPupillometryFileSectionIdentifier {
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

	protected boolean matches(IPupillometryFileLine line, PupillometryFileColumn column, String key) {
		Set<String> foundKeys = TrialAnalysisUtil.extractKeysConsideringCollapsedColumns(line, column);
		return (foundKeys != null && foundKeys.contains(key));
	}
}
