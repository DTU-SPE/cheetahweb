package org.cheetahplatform.web.eyetracking.cleaning;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingTrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.TrialConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;

public class ComputeTrialsWorkItem extends AbstractCheetahWorkItem implements IDataProcessingWorkItem {

	private String decimalSeparator;
	private String timestampColumn;
	private TrialConfiguration trialConfiguration;
	private DataProcessing dataProcessing;

	public ComputeTrialsWorkItem(long userId, Long fileId, DataProcessing dataProcessing, TrialConfiguration trialConfiguration,
			String decimalSeparator, String timestampColumn) {
		super(userId, fileId, "Computing trials.");
		this.dataProcessing = dataProcessing;
		this.trialConfiguration = trialConfiguration;
		this.decimalSeparator = decimalSeparator;
		this.timestampColumn = timestampColumn;
	}

	@Override
	public void doWork() throws Exception {
		throw new UnsupportedOperationException("Computing trials is only implemented for study data processing.");
	}

	@Override
	public boolean doWork(PupillometryFile file, DataProcessingContext context) throws Exception {
		DataProcessingTrialDetector detector = new DataProcessingTrialDetector(fileId, dataProcessing, trialConfiguration, decimalSeparator,
				timestampColumn, file);

		TrialEvaluation trialEvaluation = detector.detectTrials();
		context.setTrialEvaluation(trialEvaluation);
		return true;
	}
}
