package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.dto.PupillometryFilterDto;

public class PupillometryDataFilterRegistry {
	private static final Map<Long, IPupillometryFilter> AVAILABLE_FIlTERS;

	static {
		AVAILABLE_FIlTERS = new LinkedHashMap<Long, IPupillometryFilter>();
		AVAILABLE_FIlTERS.put(1l, new SubstitutePupilFilter(1));
		AVAILABLE_FIlTERS.put(6l, new SubstituteGazePointFilter(6));
		AVAILABLE_FIlTERS.put(2l, new StandardDeviatonFilter(2));
		AVAILABLE_FIlTERS.put(3l, new BlinkDetectionFilter(3));
		AVAILABLE_FIlTERS.put(7l, new CommandPromptPpmActivityFilter(7));
		AVAILABLE_FIlTERS.put(4l, new LinearInterpolationFilter(4));
		AVAILABLE_FIlTERS.put(5l, new ButterworthFilter(5));
	}

	public static IPupillometryFilter getFilter(Long id) {
		return AVAILABLE_FIlTERS.get(id);
	}

	public static Collection<PupillometryFilterDto> getFilterDtos() {
		List<PupillometryFilterDto> dtos = new ArrayList<PupillometryFilterDto>();
		Collection<IPupillometryFilter> values = AVAILABLE_FIlTERS.values();
		for (IPupillometryFilter iPupillometryFilter : values) {
			dtos.add(iPupillometryFilter.getDto());
		}

		return dtos;
	}
}
