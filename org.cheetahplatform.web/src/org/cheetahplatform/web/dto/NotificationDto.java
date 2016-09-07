package org.cheetahplatform.web.dto;

import java.sql.Date;

public class NotificationDto {

	private long id;
	private String message;
	private String url;
	private String type;
	private boolean read;
	private Date timestamp;

	public NotificationDto() {
		// JSON
	}

	public NotificationDto(long id, String message, String type, String url, boolean isRead, Date timestamp) {
		this.id = id;
		this.message = message;
		this.type = type;
		this.url = url;
		this.read = isRead;
		this.timestamp = timestamp;
	}

	public long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public boolean isRead() {
		return read;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setRead(boolean isRead) {
		this.read = isRead;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
