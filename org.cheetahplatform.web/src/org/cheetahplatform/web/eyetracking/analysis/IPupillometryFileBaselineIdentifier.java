package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;

public interface IPupillometryFileBaselineIdentifier {

	boolean isWithinRange(IPupillometryFileLine line);

}
