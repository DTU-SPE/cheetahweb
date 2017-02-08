package org.cheetahplatform.web.eyetracking.analysis;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public class CachedTrialDetector extends DefaultTrialDetector {

	public CachedTrialDetector(long fileId, DataProcessing dataProcessing, TrialConfiguration config, String decimalSeparator,
			String timestampColumn) {
		super(fileId, dataProcessing, config, decimalSeparator, timestampColumn);
	}

	@Override
	public PupillometryFile getFile() throws SQLException, FileNotFoundException {
		return PupillometryFileCache.INSTANCE.get(fileId);
	}
}
