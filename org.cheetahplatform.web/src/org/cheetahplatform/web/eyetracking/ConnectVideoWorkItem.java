package org.cheetahplatform.web.eyetracking;

import static org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile.extractHeader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.cheetahplatform.common.eyetracking.EyeTrackerDateCorrection;
import org.cheetahplatform.web.dao.MovieDao;
import org.cheetahplatform.web.dao.PpmInstanceDao;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ConnectRequest;
import org.cheetahplatform.web.dto.PpmInstanceDto;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

/**
 * Implementation for connecting videos to subjects.
 *
 * @author stefan.zugal
 *
 */
public class ConnectVideoWorkItem extends AbstractConnectWorkItem {

	public ConnectVideoWorkItem(long userId, long fileId, ConnectRequest request) {
		super(userId, fileId, request, "Connecting video to subject");
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto movieFile = userFileDao.getFile(fileId);
		String[] splittedFilename = splitFileName(movieFile.getFilename());
		String subjectName = splittedFilename[0];
		String experimentTaskId = splittedFilename[1];

		try (Connection connection = AbstractCheetahServlet.getDatabaseConnection()) {
			MovieDao movieDao = new MovieDao();
			if (movieDao.isFileLinkedToMovie(connection, fileId)) {
				logErrorNotification("The file '" + movieFile.getFilename() + "' is already linked to a subject.");
				return;
			}

			String tsvFilename = movieFile.getFilename().replace(".webm", "%.tsv"); // add wildcard to allow for filtered files as well
			List<UserFileDto> tsvFiles = userFileDao.getFileWithNameLike(connection, tsvFilename);
			if (tsvFiles.isEmpty()) {
				logErrorNotification("Expected exactly one file named '" + tsvFilename + "' to be connected with '"
						+ movieFile.getFilename() + "', but was " + tsvFiles.size());
				return;
			}

			long tsvFileId = tsvFiles.get(0).getId();
			File tsvFile = userFileDao.getUserFile(userFileDao.getPath(tsvFileId));
			try (BufferedReader reader = new BufferedReader(new FileReader(tsvFile))) {
				PupillometryFileHeader header = extractHeader(reader, PupillometryFile.SEPARATOR_TABULATOR);
				PupillometryFileColumn timestampColumn = header.getColumn(request.getTimestampColumn());
				if (timestampColumn == null) {
					logErrorNotification("Could not find the timestamp column named '" + request.getTimestampColumn() + "' in file '"
							+ movieFile.getFilename() + "'.");
					return;
				}
				PupillometryFileColumn localTimestampColumn = header.getColumn(LOCAL_TIMESTAMP_COLUMN_HEADER);
				if (localTimestampColumn == null) {
					logErrorNotification("Could not find local timestamp column '" + LOCAL_TIMESTAMP_COLUMN_HEADER + "' in file "
							+ movieFile.getFilename());
					return;
				}

				// extract the first line to determine the start
				List<PupillometryFileLine> content = PupillometryFile.extractContent(reader, ".", PupillometryFile.SEPARATOR_TABULATOR, 1);
				PupillometryFileLine firstLine = content.get(0);
				long timestamp = firstLine.getLong(timestampColumn) / 1000;
				String localTimestamp = firstLine.get(localTimestampColumn);
				Date startTime = EyeTrackerDateCorrection.correctDate(localTimestamp, timestamp);

				SubjectDto subject = new SubjectDao().getSubjectWithName(connection, userId, subjectName);

				// process instance id is null if no process instance id exists
				List<PpmInstanceDto> ppmInstanceDtoList = new PpmInstanceDao().selectProcessInstancesForSubjectAndTask(connection,
						subject.getSubjectId(), experimentTaskId);
				PpmInstanceDto ppmInstanceDto = null;
				Long processInstanceId = null;
				if (ppmInstanceDtoList.size() == 1) {
					ppmInstanceDto = ppmInstanceDtoList.get(0);
					processInstanceId = ppmInstanceDto.getProcessInstanceId();
				} else if (ppmInstanceDtoList.size() > 1) {
					return;
				}

				File videoFile = userFileDao.getUserFile(userFileDao.getPath(fileId));
				movieDao.insertMovie(videoFile, userId, movieFile.getFilename(), movieFile.getType(), processInstanceId,
						startTime.getTime() * 1000, subject.getSubjectId(), fileId);

				userFileDao.updateSubject(connection, movieFile.getId(), subject.getSubjectId());
				logSuccessNotification("Connected video '" + movieFile.getFilename() + "' to subject '" + subjectName + "'.");
			}
		}
	}
}
