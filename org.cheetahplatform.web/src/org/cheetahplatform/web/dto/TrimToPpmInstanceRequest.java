package org.cheetahplatform.web.dto;

import java.util.List;

public class TrimToPpmInstanceRequest {
	private List<Long> files;
	private String timestampColumn;
	private List<CodeAndExperimentActivity> activities;

	public List<CodeAndExperimentActivity> getActivities() {
		return activities;
	}

	public List<Long> getFiles() {
		return files;
	}

	public String getTimestampColumn() {
		return timestampColumn;
	}

	public void setActivities(List<CodeAndExperimentActivity> activities) {
		this.activities = activities;
	}

	public void setFiles(List<Long> files) {
		this.files = files;
	}

	public void setTimestampColumn(String timestampColumn) {
		this.timestampColumn = timestampColumn;
	}
}
