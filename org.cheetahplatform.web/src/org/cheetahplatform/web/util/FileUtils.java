package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class FileUtils {
	public static String extractSubjectName(String fileName) throws SQLException {
		String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);
		String[] splittedFilename = fileNameWithoutExtension.split(CheetahWebConstants.FILENAME_PATTERN_SEPARATOR);
		return splittedFilename[0];
	}

	public static String getFileExtension(String fileName) {
		int position = fileName.lastIndexOf(".");
		return fileName.substring(position);
	}

	public static String getFileNameWithoutExtension(String fileName) {
		int position = fileName.lastIndexOf(".");
		if (position < 0) {
			return fileName;
		}
		return fileName.substring(0, position);
	}

	public static SubjectDto getSubjectForFileName(long userId, String fileName) throws SQLException {
		String subjectName = extractSubjectName(fileName);
		try (Connection connection = AbstractCheetahServlet.getDatabaseConnection()) {
			return new SubjectDao().getSubjectWithName(connection, userId, subjectName);
		}
	}

	/**
	 * Tries to retrieve the subject's name from the database. If the subject cannot be found in the database, the filename is used to
	 * extract the subject name.
	 *
	 * @param fileName
	 *            the filename to extract the subject name if no subject is found.
	 * @param subjectId
	 *            the id of the subject
	 * @param userId
	 *            the user
	 * @return the name of the subject
	 * @throws SQLException
	 *             if an error occurs while accessing the database
	 */
	public static String getSubjectName(String fileName, Long subjectId, long userId) throws SQLException {
		SubjectDto subject = new SubjectDao().getSubjectWithId(userId, subjectId);
		if (subject != null) {
			return subject.getSubjectName();
		} else {
			if (fileName == null) {
				return "";
			}

			if (fileName.contains(CheetahWebConstants.FILENAME_PATTERN_SEPARATOR)) {
				return FileUtils.extractSubjectName(fileName);
			}
		}
		return "";
	}
}
