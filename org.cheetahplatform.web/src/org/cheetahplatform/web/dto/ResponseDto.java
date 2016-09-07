package org.cheetahplatform.web.dto;

public class ResponseDto {
	private String error;

	public ResponseDto(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
