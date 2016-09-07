package org.cheetahplatform.web.dto;

import java.util.List;

public class CalculateAverageLoadForPpmInstanceRequest {
	private List<Long> ppmInstanceIds;

	public CalculateAverageLoadForPpmInstanceRequest() {
		// JSON
	}

	public List<Long> getPpmInstanceIds() {
		return ppmInstanceIds;
	}

	public void setPpmInstanceIds(List<Long> ppmInstanceIds) {
		this.ppmInstanceIds = ppmInstanceIds;
	}
}
