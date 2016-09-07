package org.cheetahplatform.web.dto;

public class CheetahWorkerDto {
	private String message;
	private String status;

	public CheetahWorkerDto(String message, String status) {
		this.message = message;
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public String getStatus() {
		return status;
	}

	public void setMessage(String status) {
		this.message = status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
