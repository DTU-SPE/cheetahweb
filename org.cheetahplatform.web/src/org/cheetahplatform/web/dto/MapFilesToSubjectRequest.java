package org.cheetahplatform.web.dto;

import java.util.Map;

public class MapFilesToSubjectRequest {
	private Map<Long, Long> filesToSubjectIds;

	public Map<Long, Long> getFilesToSubjectIds() {
		return filesToSubjectIds;
	}

	public void setFilesToSubjectIds(Map<Long, Long> filesToSubjectIds) {
		this.filesToSubjectIds = filesToSubjectIds;
	}
}
