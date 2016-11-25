package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.util.FileUtils;

public class ExecuteDataProcessingWorkItem extends AbstractCheetahWorkItem {
	private DataProcessing processing;
	private List<IDataProcessingWorkItem> subWorkItems;

	public ExecuteDataProcessingWorkItem(long userId, long fileId, DataProcessing processing) {
		super(userId, fileId, "Executing study data processing: " + processing.getName());
		this.processing = processing;
		subWorkItems = new ArrayList<>();
	}

	public void addDataProcessingWorkItem(IDataProcessingWorkItem dataProcessingWorkItem) {
		subWorkItems.add(dataProcessingWorkItem);
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto originalFileDto = userFileDao.getFile(fileId);
		String path = userFileDao.getPath(fileId);
		File file = userFileDao.getUserFile(path);

		PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true,
				processing.getDecimalSeparator());
		String preProcessingErrors = runPreProcessing(originalFileDto, pupillometryFile);
		if (preProcessingErrors != null) {
			logErrorNotification(preProcessingErrors);
			return;
		}

		DataProcessingContext context = new DataProcessingContext();
		for (IDataProcessingWorkItem subItem : subWorkItems) {
			try {
				if (!subItem.doWork(pupillometryFile, context)) {
					List<DataProcessingResult> results = context.getResults();
					for (DataProcessingResult dataProcessingResult : results) {
						if (dataProcessingResult.isError()) {
							String notification = "An error occured while running the data processing routine on file '"
									+ originalFileDto.getFilename() + "'. The follwing error occured: " + dataProcessingResult.getMessage();
							if (dataProcessingResult.getAdditionalInformation() != null) {
								notification = notification + " (" + dataProcessingResult.getAdditionalInformation() + ")";
							}
							logErrorNotification(notification);
							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logErrorNotification("An unexpected error occurred while processing file '" + originalFileDto.getFilename() + "'.");
				return;
			}
		}

		writeProcessedFile(userFileDao, originalFileDto, pupillometryFile);
	}

	private String runPreProcessing(UserFileDto originalFileDto, PupillometryFile pupillometryFile) throws IOException, SQLException {
		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn timestampColumn = header.getColumn(processing.getTimestampColumn());
		if (timestampColumn == null) {
			return "Could not find timestamp column '" + processing.getTimestampColumn() + "' in file " + originalFileDto.getFilename();
		}

		PupillometryFileColumn leftPupilColumn = header.getColumn(processing.getLeftPupilColumn());
		if (leftPupilColumn == null) {
			return "Could not find left pupil column '" + processing.getLeftPupilColumn() + "' in file " + originalFileDto.getFilename();
		}

		PupillometryFileColumn rightPupilColumn = header.getColumn(processing.getRightPupilColumn());
		if (rightPupilColumn == null) {
			return "Could not find right pupil column '" + processing.getRightPupilColumn() + "' in file " + originalFileDto.getFilename();
		}

		// do some pre-processing
		pupillometryFile.collapseEmptyColumns(timestampColumn);
		pupillometryFile.removeNullValues("-1");
		pupillometryFile.adaptTimestamps(timestampColumn);
		return null;
	}

	private void writeProcessedFile(UserFileDao userFileDao, UserFileDto originalFileDto, PupillometryFile pupillometryFile)
			throws SQLException, IOException {
		String newName = null;
		String fileName = originalFileDto.getFilename();
		String subjectName = FileUtils.getSubjectName(fileName, originalFileDto.getSubjectId(), userId);
		if (!subjectName.trim().isEmpty()) {
			subjectName = subjectName + CheetahWebConstants.FILENAME_PATTERN_SEPARATOR;
		}

		// no need to add subject name if name is already there :)
		if (fileName.startsWith(subjectName)) {
			subjectName = "";
		}
		int position = fileName.lastIndexOf(".");
		newName = subjectName + fileName.substring(0, position) + CheetahWebConstants.FILENAME_PATTERN_SEPARATOR + "study_data_processed"
				+ fileName.substring(position);

		String relativePath = userFileDao.generateRelativePath(userId, newName);

		String absolutePath = userFileDao.getAbsolutePath(relativePath);
		pupillometryFile.writeToFile(new File(absolutePath));

		String comment = "Executed data processing: '" + processing.getName() + "'.";
		long newFileId = userFileDao.insertUserFile(userId, newName, relativePath, originalFileDto.getType(), comment, null,
				originalFileDto.getSubjectId(), false, null);
		logSuccessNotification("Successfully processed a file as part of a study data processing! Processed pupillometry file: " + newName);
		userFileDao.addTags(newFileId, UserFileDao.TAG_STUDY_DATA_PROCESSED);
	}
}
