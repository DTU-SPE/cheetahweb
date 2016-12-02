package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.eclipse.core.runtime.Assert;

public class StartAndEndPupillometryFileSectionIdentifier extends StartPupillometryFileSectionIdentifier {
	private String end;
	private boolean endSceneStarted = false;

	public StartAndEndPupillometryFileSectionIdentifier(String start, String end) {
		super(start);
		Assert.isNotNull(end);
		Assert.isLegal(!end.trim().isEmpty());
		this.end = end;
	}

	@Override
	public boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn column) {
		if (!started) {
			return false;
		}

		boolean matches = matches(line, column, end);
		if (matches) {
			// make sure the end scene is included
			if (!endSceneStarted) {
				endSceneStarted = true;
				return false;
			} else {
				started = false;
				endSceneStarted = false;
				return true;
			}
		}
		return matches;
	}

	@Override
	public boolean isStart(IPupillometryFileLine line, PupillometryFileColumn column) {
		if (started) {
			return false;
		}

		boolean isStart = super.isStart(line, column);

		// handle special case where start === end
		if (isStart && start.equals(end)) {
			endSceneStarted = true;
		}

		return isStart;
	}
}