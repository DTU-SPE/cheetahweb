package org.cheetahplatform.web.dto;

import java.util.List;

public class UpdateUserDataTagsRequest {
	private List<Long> fileIds;
	private List<String> tags;

	public UpdateUserDataTagsRequest() {
		// JSON
	}

	public List<Long> getFileIds() {
		return fileIds;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setFileIds(List<Long> fileIds) {
		this.fileIds = fileIds;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
