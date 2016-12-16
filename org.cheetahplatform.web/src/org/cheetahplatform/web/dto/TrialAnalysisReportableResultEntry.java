package org.cheetahplatform.web.dto;

public class TrialAnalysisReportableResultEntry extends ReportableResultEntry {
	private final int trialNumber;

	public TrialAnalysisReportableResultEntry(int trialNumber, String result) {
		super(result);
		this.trialNumber = trialNumber;
	}

	public int getTrialNumber() {
		return trialNumber;
	}

	@Override
	public boolean isSameResult(int trialNumber) {
		return this.trialNumber == trialNumber;
	}
}
