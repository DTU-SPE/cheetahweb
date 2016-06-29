package org.cheetahplatform.web.dto;

import java.util.List;

public class PercentilesDto {
	private long sessionStart;
	private List<PercentileDto> percentiles;

	public long getSessionStart() {
		return sessionStart;
	}

	public void setSessionStart(long sessionStart) {
		this.sessionStart = sessionStart;
	}

	public List<PercentileDto> getPercentiles() {
		return percentiles;
	}

	public void setPercentiles(List<PercentileDto> percentiles) {
		this.percentiles = percentiles;
	}

}
