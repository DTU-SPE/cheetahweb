package org.cheetahplatform.web.eyetracking.cleaning;

public class DataProcessingResult {
	private String message;
	private String additionalInformation;

	private boolean error;

	public DataProcessingResult(String message) {
		this(message, false);
	}

	public DataProcessingResult(String message, boolean error) {
		this.error = error;
		this.message = message;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public String getMessage() {
		return message;
	}

	public boolean isError() {
		return error;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
