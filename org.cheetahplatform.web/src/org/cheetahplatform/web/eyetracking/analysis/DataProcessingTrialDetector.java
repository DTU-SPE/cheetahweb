package org.cheetahplatform.web.eyetracking.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public class DataProcessingTrialDetector extends AbstractTrialDetector {
	private PupillometryFile pupillometryFile;

	public DataProcessingTrialDetector(long fileId, TrialConfiguration config, String decimalSeparator, String timestampColumn,
			PupillometryFile pupillometryFile) {
		super(fileId, config, decimalSeparator, timestampColumn);
		this.pupillometryFile = pupillometryFile;
	}

	@Override
	protected PupillometryFile loadPupillometryFile() throws SQLException, FileNotFoundException, IOException {
		return pupillometryFile;
	}
}
