package org.cheetahplatform.web.eyetracking;

import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.parseDouble;
import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.cheetahplatform.common.eyetracking.EyeTrackerDateCorrection;
import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.PpmInstanceDao;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ConnectRequest;
import org.cheetahplatform.web.dto.PpmInstanceDto;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;
import org.cheetahplatform.web.util.FileUtils;

/**
 * Work item responsible for establishing a connection between pupillometry data and subjects.
 *
 * @author stefan.zugal
 *
 */
public class ConnectPupillometricDataWorkItem extends AbstractConnectWorkItem {
	private static final String PUPIL_RIGHT = "PupilRight";
	private static final String PUPIL_LEFT = "PupilLeft";
	private static final String SEPARATOR = ";";
	private static final String EYE_TRACKER_TIMESTAMP = "EyeTrackerTimestamp";

	public ConnectPupillometricDataWorkItem(long userId, long fileId, ConnectRequest request) {
		super(userId, fileId, request, "Connecting pupillometry data to subject");
		this.fileId = fileId;
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto file = userFileDao.getFile(fileId);
		File inputFile = userFileDao.getUserFile(userFileDao.getPath(fileId));

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(inputFile))));
				Connection connection = AbstractCheetahServlet.getDatabaseConnection()) {
			String header = reader.readLine();
			List<String> headerColumns = split(header, '\t');

			int leftIndex = -1;
			int rightIndex = -1;
			int timestampIndex = -1;
			int localTimestampColumnIndex = -1;

			for (int i = 0; i < headerColumns.size(); i++) {
				String column = headerColumns.get(i);
				if (column.equals(request.getLeftPupilColumn())) {
					leftIndex = i;
				} else if (column.equals(request.getRightPupilColumn())) {
					rightIndex = i;
				} else if (column.equals(request.getTimestampColumn())) {
					timestampIndex = i;
				} else if (column.equals(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_LOCAL_TIMESTAMP)) {
					localTimestampColumnIndex = i;
				}
			}

			if (leftIndex < 0) {
				logErrorNotification(
						"Could not find left pupil column '" + request.getLeftPupilColumn() + "' in file '" + file.getFilename() + "'.");
				return;
			}
			if (rightIndex < 0) {
				logErrorNotification(
						"Could not find right pupil column '" + request.getRightPupilColumn() + "' in file '" + file.getFilename() + "'.");
				return;
			}
			if (timestampIndex < 0) {
				logErrorNotification(
						"Could not find timestamp column '" + request.getTimestampColumn() + "' in file '" + file.getFilename() + "'.");
				return;
			}

			String[] splitted = splitFileName(file.getFilename());
			if (splitted == null) {
				return;
			}
			String subjectName = splitted[0];
			String experimentTask = splitted[1];
			SubjectDto subject = new SubjectDao().getSubjectWithName(connection, userId, subjectName);
			if (subject == null) {
				return;
			}

			String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(file.getFilename());
			String newName = fileNameWithoutExtension + ".csv";
			String relativePath = userFileDao.generateRelativePath(userId, newName);
			String absolutePath = userFileDao.getAbsolutePath(relativePath);

			try (FileWriter writer = new FileWriter(new File(absolutePath))) {
				writer.write(EYE_TRACKER_TIMESTAMP + SEPARATOR + PUPIL_LEFT + SEPARATOR + PUPIL_RIGHT + "\n");

				String line = reader.readLine();
				while (line != null) {
					List<String> token = split(line, '\t');

					String timestamp = token.get(timestampIndex);
					if (timestamp != null && !timestamp.trim().isEmpty()) {
						// convert only if the timestamp is available
						if (localTimestampColumnIndex > -1) {
							// remove nano seconds for conversion
							long parsedTimestamp = Long.parseLong(timestamp) / 1000;

							String localTimestamp = token.get(localTimestampColumnIndex);
							try {
								Date correctDate = EyeTrackerDateCorrection.correctDate(localTimestamp, parsedTimestamp);
								long correctedTime = correctDate.getTime();
								correctedTime *= 1000;
								timestamp = String.valueOf(correctedTime);
							} catch (ParseException e) {
								logErrorNotification("Unable to parse local timestamp: " + localTimestamp);
								return;
							}
						}
					}

					double left = parseDouble(token.get(leftIndex).replaceAll(",", "."));
					double right = parseDouble(token.get(rightIndex).replaceAll(",", "."));

					String outputLine = timestamp + SEPARATOR + left + SEPARATOR + right + "\n";
					writer.write(outputLine);
					line = reader.readLine();
				}
			}

			List<PpmInstanceDto> ppmInstanceDtoList = new PpmInstanceDao().selectProcessInstancesForSubjectAndTask(connection,
					subject.getSubjectId(), experimentTask);
			PpmInstanceDto ppmInstanceDto = null;
			Long processInstanceId = null;
			if (ppmInstanceDtoList.size() == 1) {
				ppmInstanceDto = ppmInstanceDtoList.get(0);
				processInstanceId = ppmInstanceDto.getProcessInstanceId();
			} else if (ppmInstanceDtoList.size() > 1) {
				return;
			}

			String newFilename = fileNameWithoutExtension + ".csv";
			if (processInstanceId != null) {
				PpmInstanceDto ppmInstance = new PpmInstanceDao().selectPpmInstance(processInstanceId);
				String comment = "Connected to PPM instance with id '" + ppmInstance.getProcessInstanceId() + "'.";
				userFileDao.insertUserFile(userId, newFilename, relativePath, file.getType(), comment, ppmInstance.getProcessInstanceId(),
						subject.getSubjectId(), true, fileId);
				EyeTrackingCache.INSTANCE.invalidateCache(ppmInstance.getProcessInstanceId());
				logSuccessNotification(
						"Connected '" + file.getFilename() + "' to PPM instance with id '" + ppmInstance.getProcessInstanceId() + "'.");
			} else {
				userFileDao.insertUserFile(userId, newFilename, relativePath, file.getType(), "Compressed and added to subject.", null,
						subject.getSubjectId(), true, fileId);
				logSuccessNotification("Connected '" + file.getFilename() + "' with subject '" + subjectName + "'.");
			}
		}
	}

}
