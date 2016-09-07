package org.cheetahplatform.web.dto;

import java.util.List;

public class MoveWorkItemToTopRequest {
	private List<CheetahWorkItemDto> workItems;

	public MoveWorkItemToTopRequest() {
		// json
	}

	public List<CheetahWorkItemDto> getWorkItems() {
		return workItems;
	}

	public void setWorkItems(List<CheetahWorkItemDto> workItems) {
		this.workItems = workItems;
	}
}
