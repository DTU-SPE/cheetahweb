package org.cheetahplatform.web.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.cheetahplatform.web.eyetracking.analysis.AbstractTrialDetector;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.TrialConfiguration;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;

public class StandaloneTrialDetector extends AbstractTrialDetector {

	private String filePath;
	private PupillometryFile pupillometryFile;

	public StandaloneTrialDetector(DataProcessing dataProcessing, TrialConfiguration config, String filePath) {
		super(-1, dataProcessing, config, dataProcessing.getDecimalSeparator(), dataProcessing.getTimestampColumn());
		this.filePath = filePath;
	}

	public PupillometryFile getPupillometryFile() {
		return pupillometryFile;
	}

	@Override
	protected PupillometryFile loadPupillometryFile() throws SQLException, FileNotFoundException, IOException {
		File file = new File(filePath);
		pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true, dataProcessing.getDecimalSeparator());
		PupillometryFileHeader header = pupillometryFile.getHeader();
		pupillometryFile.collapseEmptyLines(header.getColumn(timestampColumn));
		return pupillometryFile;
	}
}
