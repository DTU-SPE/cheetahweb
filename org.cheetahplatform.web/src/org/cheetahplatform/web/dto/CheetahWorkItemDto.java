package org.cheetahplatform.web.dto;

public class CheetahWorkItemDto {
	private String message;
	private boolean editable;
	private long id;

	public CheetahWorkItemDto() {
		// JSON
	}

	public CheetahWorkItemDto(long id, String message, boolean cancelable) {
		this.id = id;
		this.message = message;
		this.editable = cancelable;
	}

	public long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean cancelable) {
		this.editable = cancelable;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
