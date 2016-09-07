package org.cheetahplatform.web.dto;

import java.util.List;

public class ListExperimentActivitiesRequest {
	private List<Long> files;

	public List<Long> getFiles() {
		return files;
	}

	public void setFiles(List<Long> files) {
		this.files = files;
	}

}
