package org.cheetahplatform.web.eyetracking.cleaning;

public class PupillometryParameter {
	private String key;
	private String description;
	private String defaultValue;
	/**
	 * <code>true</code> if this column defines a column in the pupil data, <code>false</code> if it represents additional information
	 * (e.g., sampling rate).
	 */
	private boolean isDataColumn;

	public PupillometryParameter() {
		// JSON
	}

	public PupillometryParameter(String key, String description, String defaultValue, boolean isDataColumn) {
		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
		this.isDataColumn = isDataColumn;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public String getKey() {
		return key;
	}

	public boolean isDataColumn() {
		return isDataColumn;
	}

	public void setDataColumn(boolean isDataColumn) {
		this.isDataColumn = isDataColumn;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
