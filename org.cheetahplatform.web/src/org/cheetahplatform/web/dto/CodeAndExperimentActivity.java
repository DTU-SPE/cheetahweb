package org.cheetahplatform.web.dto;

public class CodeAndExperimentActivity {
	private long code;
	private String activity;

	public String getActivity() {
		return activity;
	}

	public long getCode() {
		return code;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public void setCode(long code) {
		this.code = code;
	}
}
