package org.cheetahplatform.web;

public class CheetahWorkItemStatus {
	private long id;
	private String status;

	public CheetahWorkItemStatus(long id, String status) {
		this.id = id;
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
