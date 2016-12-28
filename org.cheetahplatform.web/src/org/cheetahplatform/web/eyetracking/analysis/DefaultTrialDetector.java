package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;

public class DefaultTrialDetector extends AbstractTrialDetector {
	public DefaultTrialDetector(long fileId, DataProcessing dataProcessing, TrialConfiguration config, String decimalSeparator,
			String timestampColumn) {
		super(fileId, dataProcessing, config, decimalSeparator, timestampColumn);
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
		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn timeStamp = header.getColumn(timestampColumn);
		pupillometryFile.collapseEmptyLines(timeStamp);

		if (header.hasColumn(CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA)) {
			PupillometryFileColumn studioEventDataColumn = header.getColumn(CleanPupillometryDataWorkItem.STUDIO_EVENT_DATA);
			pupillometryFile.addSceneColumn(studioEventDataColumn);
		}

		return pupillometryFile;
	}
}
