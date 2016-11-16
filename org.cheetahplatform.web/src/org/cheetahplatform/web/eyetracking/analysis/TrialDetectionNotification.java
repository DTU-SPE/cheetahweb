package org.cheetahplatform.web.eyetracking.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cheetahplatform.common.Assert;

public class TrialDetectionNotification {
	public static final String TYPE_ERROR = "danger";
	public static final String TYPE_WARNING = "warning";
	public static final String TYPE_INFO = "info";

	private String message;
	private String type;
	private Map<String, String> data;

	public TrialDetectionNotification() {
		// JSON
	}

	public TrialDetectionNotification(String message, String type) {
		Assert.isNotNull(message);
		Assert.isNotNull(type);
		this.message = message;
		this.type = type;

		data = new HashMap<>();
	}

	public Map<String, String> getData() {
		return data;
	}

	public String getMessage() {
		return message;
	}

	public String getType() {
		return type;
	}

	public void setData(Map<String, String> data) {
		this.data = Collections.unmodifiableMap(data);
	}

	public void setDate(String key, String value) {
		data.put(key, value);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setType(String type) {
		this.type = type;
	}
}
