package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;

public class DefaultTrialDetector extends AbstractTrialDetector {
	public DefaultTrialDetector(long fileId, DataProcessing dataProcessing, TrialConfiguration config, String decimalSeparator,
			String timestampColumn) {
		super(fileId, dataProcessing, config, decimalSeparator, timestampColumn);
	}

	protected PupillometryFile getFile() throws SQLException, FileNotFoundException {
		UserFileDao userFileDao = new UserFileDao();
		String filePath = userFileDao.getPath(fileId);
		File file = userFileDao.getUserFile(filePath);
		return new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true, decimalSeparator);
	}

	/**
	 * Loads the pupillometry file defined in this detector.
	 *
	 * @return
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Override
	public PupillometryFile loadPupillometryFile() throws SQLException, FileNotFoundException, IOException {
		PupillometryFile pupillometryFile = getFile();
		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn timeStamp = header.getColumn(timestampColumn);
		pupillometryFile.collapseEmptyLines(timeStamp);

		if (header.hasColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_STUDIO_EVENT_DATA)) {
			PupillometryFileColumn studioEventDataColumn = header.getColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_STUDIO_EVENT_DATA);
			pupillometryFile.addSceneColumn(studioEventDataColumn);
		}

		return pupillometryFile;
	}
}
