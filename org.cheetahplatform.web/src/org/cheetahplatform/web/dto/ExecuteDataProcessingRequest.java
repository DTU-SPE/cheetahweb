package org.cheetahplatform.web.dto;

import java.util.List;

public class ExecuteDataProcessingRequest {
	private List<Long> fileIds;
	private long dataProcessingId;
	private long studyId;

	public long getDataProcessingId() {
		return dataProcessingId;
	}

	public List<Long> getFileIds() {
		return fileIds;
	}

	public long getStudyId() {
		return studyId;
	}

	public void setDataProcessingId(long dataProcessingId) {
		this.dataProcessingId = dataProcessingId;
	}

	public void setFileIds(List<Long> fileIds) {
		this.fileIds = fileIds;
	}

	public void setStudyId(long studyId) {
		this.studyId = studyId;
	}
}
