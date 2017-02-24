package org.cheetahplatform.web;

import static org.cheetahplatform.modeler.ModelerConstants.ATTRIBUTE_EXPERIMENT_ACTIVITY_ID;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.cheetahplatform.common.eyetracking.EyeTrackerDateCorrection;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.CodeAndExperimentActivity;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

/**
 * Work item that takes a pupillometry file and splits it based on the experiment. For instance, an experiment may consist of a modeling
 * session and a working memory test - this class is responsible for splitting the file respectively.
 *
 * @author stefan.zugal
 *
 */
public class TrimWorkItem extends AbstractActivityBasedWorkItem {

	public TrimWorkItem(long userId, long fileId, String timestampColumn, List<CodeAndExperimentActivity> activities) {
		super(userId, fileId, activities, timestampColumn, "Trimming Pupillometric Data");
	}

	@Override
	protected void processExperimentActivity(String subjectName, UserFileDto fileDto, PupillometryFile reader,
			List<PupillometryFileLine> content, PupillometryFileColumn timestampColumn, PupillometryFileColumn localTimestampColumn,
			AuditTrailEntry entry, Date activityStart, Date activityEnd, Connection connection) throws Exception {
		int addedLineCount = 0;
		PupillometryFile trimmed = reader.emptyCopy();
		for (PupillometryFileLine line : content) {
			String timestamp = line.get(timestampColumn);
			if (timestamp != null && !timestamp.isEmpty()) {
				long parsedTimestamp = Long.parseLong(timestamp) / 1000;

				String localTimestamp = line.get(localTimestampColumn);
				Date date = EyeTrackerDateCorrection.correctDate(localTimestamp, parsedTimestamp);
				if (date.after(activityStart) && date.before(activityEnd)) {
					trimmed.appendLine(line);
					addedLineCount++;
				}
			}
		}

		UserFileDao userFileDao = new UserFileDao();
		String fileName = fileDto.getFilename();
		int position = fileName.lastIndexOf(".");
		String activityId = entry.getAttribute(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID);
		String newName = subjectName + "@" + activityId + fileName.substring(position);
		String relativePath = userFileDao.generateRelativePath(userId, newName);

		String absolutePath = userFileDao.getAbsolutePath(relativePath);
		trimmed.writeToFile(new File(absolutePath));
		String message = "Proband: " + subjectName + "; Activity: " + activityId + "; Lines added: " + addedLineCount;
		long trimmedFileId = userFileDao.insertUserFile(userId, newName, relativePath, fileDto.getType(), message, fileDto.getSubjectId());
		userFileDao.addTags(trimmedFileId, UserFileDao.TAG_TRIMMED);

		logSuccessNotification("Created trimmed file " + newName + "; " + message);
	}

}
