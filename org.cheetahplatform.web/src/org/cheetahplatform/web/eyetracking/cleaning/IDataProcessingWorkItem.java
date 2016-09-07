package org.cheetahplatform.web.eyetracking.cleaning;

public interface IDataProcessingWorkItem {
	public void doWork(PupillometryFile file, DataProcessingContext context) throws Exception;
}
