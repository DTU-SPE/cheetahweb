package org.cheetahplatform.web.dto;

import org.cheetahplatform.web.eyetracking.analysis.TrialConfiguration;

public class ComputeTrialsRequest {
	private TrialConfiguration config;
	private long fileId;
	private String timestampColumn;
	private String decimalSeparator;

	public TrialConfiguration getConfig() {
		return config;
	}

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public long getFileId() {
		return fileId;
	}

	public String getTimestampColumn() {
		return timestampColumn;
	}

	public void setConfig(TrialConfiguration config) {
		this.config = config;
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public void setTimestampColumn(String timestampColumn) {
		this.timestampColumn = timestampColumn;
	}
}
