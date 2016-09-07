package org.cheetahplatform.web.eyetracking;

public interface IEyeTrackingDataSource {
	CachedEyeTrackingData load(long userFile) throws Exception;
}
