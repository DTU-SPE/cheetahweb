package org.cheetahplatform.web.dto;

public class ReportableResultEntry {
	protected final String result;

	public ReportableResultEntry(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public boolean isSameResult(int trialNumber) {
		return true;
	}
}