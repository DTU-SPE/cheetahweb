package org.cheetahplatform.web.dto;

import org.cheetahplatform.web.dao.UserFileDao;

public class MovieDto {
	private String url;
	private String type;
	private long id;
	private long startTimestamp;
	private long processInstanceId;
	private long sessionStartTimestamp;

	public MovieDto(String url, String type, long movieId, long startTimestamp, long processInstance) {
		this.url = url;
		this.type = type;
		this.id = movieId;
		this.startTimestamp = startTimestamp;
		this.processInstanceId = processInstance;
	}

	public long getId() {
		return id;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public long getSessionStartTimestamp() {
		return sessionStartTimestamp;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public void removeWebAppsPrefix() {
		if (url.startsWith(UserFileDao.WEBAPPS_PATH)) {
			url = url.replaceFirst(UserFileDao.WEBAPPS_PATH, "");
		}
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public void setSessionStartTimestamp(long sessionStartTimestamp) {
		this.sessionStartTimestamp = sessionStartTimestamp;
	}

	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
