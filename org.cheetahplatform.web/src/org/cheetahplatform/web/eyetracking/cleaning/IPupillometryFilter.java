package org.cheetahplatform.web.eyetracking.cleaning;

import org.cheetahplatform.web.dto.FilterRequest;
import org.cheetahplatform.web.dto.PupillometryFilterDto;

public interface IPupillometryFilter {

	PupillometryFilterDto getDto();

	long getId();

	String getName();

	String run(FilterRequest request, PupillometryFile file) throws Exception;
}
