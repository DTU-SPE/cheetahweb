package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Set;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public abstract class AbstractPupillometryFileSectionIdentifier implements IPupillometryFileSectionIdentifier {
	protected boolean matches(IPupillometryFileLine line, PupillometryFileColumn column, String key) {
		Set<String> foundKeys = TrialAnalysisUtil.extractKeysConsideringCollapsedColumns(line, column);
		return (foundKeys != null && foundKeys.contains(key));
	}
}
