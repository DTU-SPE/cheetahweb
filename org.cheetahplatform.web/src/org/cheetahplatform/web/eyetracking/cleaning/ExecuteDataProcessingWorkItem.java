package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;

public class ExecuteDataProcessingWorkItem extends AbstractCheetahWorkItem {

	private DataProcessing processing;
	private long fileId;
	private List<IDataProcessingWorkItem> subWorkItems;

	public ExecuteDataProcessingWorkItem(long userId, long fileId, DataProcessing processing) {
		super(userId, "Executing study data processing: " + processing.getName());
		this.fileId = fileId;
		this.processing = processing;
		subWorkItems = new ArrayList<>();
	}

	public void addDataProcessingWorkItem(IDataProcessingWorkItem dataProcessingWorkItem) {
		subWorkItems.add(dataProcessingWorkItem);
	}

	@Override
	public void doWork() throws Exception {

	}
}
