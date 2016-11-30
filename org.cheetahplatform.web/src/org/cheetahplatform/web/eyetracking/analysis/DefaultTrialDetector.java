package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class DefaultTrialDetector extends AbstractTrialDetector {
	public DefaultTrialDetector(long fileId, TrialConfiguration config, String decimalSeparator, String timestampColumn) {
		super(fileId, config, decimalSeparator, timestampColumn);
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
		UserFileDao userFileDao = new UserFileDao();
		String filePath = userFileDao.getPath(fileId);
		File file = userFileDao.getUserFile(filePath);
		PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true, decimalSeparator);
		PupillometryFileColumn timeStamp = pupillometryFile.getHeader().getColumn(timestampColumn);
		pupillometryFile.collapseEmptyLines(timeStamp);
		return pupillometryFile;
	}
}
