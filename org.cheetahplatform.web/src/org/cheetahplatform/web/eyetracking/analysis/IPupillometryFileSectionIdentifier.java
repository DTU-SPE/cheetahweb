package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public interface IPupillometryFileSectionIdentifier {
	boolean isEnd(IPupillometryFileLine line, PupillometryFileColumn column);

	boolean isStart(IPupillometryFileLine line, PupillometryFileColumn column);
}
