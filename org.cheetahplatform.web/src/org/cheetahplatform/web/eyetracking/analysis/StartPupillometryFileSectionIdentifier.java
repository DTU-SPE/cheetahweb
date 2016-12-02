package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.eclipse.core.runtime.Assert;

public class StartPupillometryFileSectionIdentifier extends AbstractPupillometryFileSectionIdentifier {
	protected String start;
	protected boolean started = false;

	public StartPupillometryFileSectionIdentifier(String start) {
		Assert.isNotNull(start);
		Assert.isLegal(!start.trim().isEmpty());
		this.start = start;
	}

	@Override
	public boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn column) {
		// run until the end
		return false;
	}

	@Override
	public boolean isStart(IPupillometryFileLine line, PupillometryFileColumn column) {
		boolean matches = matches(line, column, start);
		if (matches) {
			if (started) {
				matches = false;
			}
			started = !started;
		}
		return matches;
	}
}
