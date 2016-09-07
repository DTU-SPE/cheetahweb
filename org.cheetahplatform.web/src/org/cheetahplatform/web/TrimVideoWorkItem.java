package org.cheetahplatform.web;

import static org.cheetahplatform.modeler.ModelerConstants.ATTRIBUTE_EXPERIMENT_ACTIVITY_ID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.CodeAndExperimentActivity;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

/**
 * Work item for trimming videos.
 *
 * @author stefan.zugal
 *
 */
public class TrimVideoWorkItem extends AbstractActivityBasedWorkItem {

	private static final String[] FFMPEG_LOCATIONS = new String[] { "C:\\Program Files\\ffmpeg\\bin", "/usr/local/lib/ffmpeg/64bit" };

	public TrimVideoWorkItem(long userId, long fileId, List<CodeAndExperimentActivity> activities, String timestampColumn) {
		super(userId, fileId, activities, timestampColumn, "Trimming Video");
	}

	private String formatTimestamp(long timestamp) {
		long ms = timestamp % 1000;
		long seconds = (timestamp / 1000) % 60;
		long minutes = (timestamp / (1000 * 60)) % 60;
		long hours = timestamp / (1000 * 60 * 60);

		DecimalFormat format = new DecimalFormat("00");
		DecimalFormat msFormat = new DecimalFormat("000");
		return format.format(hours) + ":" + format.format(minutes) + ":" + format.format(seconds) + "." + msFormat.format(ms);
	}

	/**
	 * Loads the file to be processed from the disk. Takes the video file as input and tries to guess the corresponding eyetracking session.
	 *
	 * @return
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	@Override
	protected PupillometryFile loadPupillometryFile(Connection connection) throws SQLException, FileNotFoundException {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto userFile = userFileDao.getFile(fileId);
		String[] splittedFilename = splitFileName(userFile.getFilename());
		String subjectName = splittedFilename[0];
		String experiment = splittedFilename[1];

		String expectedName = subjectName + "@" + experiment + ".tsv";
		List<UserFileDto> eyetrackingFiles = userFileDao.getFileByName(connection, expectedName);
		if (eyetrackingFiles.isEmpty()) {
			logErrorNotification("Could not find the following file: " + expectedName + ". This file is required for trimming video "
					+ userFile.getFilename());
			return null;
		} else if (eyetrackingFiles.size() > 1) {
			logErrorNotification(
					"Found multiple files named " + expectedName + ". Please make sure there is only one file with this name.");
			return null;
		}

		long eyetrackingId = eyetrackingFiles.get(0).getId();
		File eyetrackingFile = userFileDao.getUserFile(userFileDao.getPath(eyetrackingId));
		return new PupillometryFile(eyetrackingFile, PupillometryFile.SEPARATOR_TABULATOR, true, ".");
	}

	@Override
	protected void processExperimentActivity(String subjectName, UserFileDto fileDto, PupillometryFile file,
			List<PupillometryFileLine> content, PupillometryFileColumn timestampColumn, PupillometryFileColumn localTimestampColumn,
			AuditTrailEntry entry, Date activityStart, Date activityEnd, Connection connection) throws Exception {
		Date sessionStart = computeTimestamp(timestampColumn, localTimestampColumn, content, 0);
		long activityOffset = activityStart.getTime() - sessionStart.getTime();
		long activityDuration = activityEnd.getTime() - activityStart.getTime();

		trimVideo(entry, activityOffset, activityDuration);
	}

	/**
	 * Trims the video.
	 *
	 * @param activityOffset
	 *            the offset at which trimming should start
	 * @param activityDuration
	 *            the duration to which the video should be trimmed
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void trimVideo(AuditTrailEntry entry, long activityOffset, long activityDuration)
			throws SQLException, IOException, InterruptedException {
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		String ffmpeg = "ffmpeg";
		if (isWindows) {
			ffmpeg = "ffmpeg.exe";
		}

		File ffmpegExecutable = null;
		for (String location : FFMPEG_LOCATIONS) {
			File tmpLocation = new File(location, ffmpeg);
			if (tmpLocation.exists()) {
				ffmpegExecutable = tmpLocation;
				break;
			}
		}

		if (ffmpegExecutable == null) {
			logErrorNotification("Could not find the ffmpeg-executable, checked the following locations: " + FFMPEG_LOCATIONS
					+ ". If you have ffmpeg installed, you may want to add your path to " + TrimVideoWorkItem.class.getName()
					+ ".FFMPEG_LOCATIONS.");
			return;
		}

		List<String> arguments = new ArrayList<>();
		if (isWindows) {
			arguments.add("\"" + ffmpegExecutable.getAbsolutePath() + "\"");
		} else {
			arguments.add(ffmpegExecutable.getAbsolutePath());
		}
		arguments.add("-y");
		arguments.add("-ss");
		arguments.add(formatTimestamp(activityOffset));
		arguments.add("-t");
		arguments.add(formatTimestamp(activityDuration));

		UserFileDao userFileDao = new UserFileDao();
		File videoFile = userFileDao.getUserFile(userFileDao.getPath(fileId));
		arguments.add("-i");
		arguments.add(videoFile.getAbsolutePath());
		File videoOutput = File.createTempFile("cheetah_trim_video_work_item", ".webm");
		arguments.add(videoOutput.getAbsolutePath());

		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		processBuilder.redirectErrorStream(true);
		Process trimmingProcess = processBuilder.start();

		// read the content from the command line (otherwise will cause the process to stall)
		try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(trimmingProcess.getInputStream()))) {
			String currentLine = inputStream.readLine();
			while (currentLine != null) {
				currentLine = inputStream.readLine();
			}
		}
		trimmingProcess.waitFor();

		UserFileDto sourceFile = userFileDao.getFile(fileId);
		SubjectDto subject = new SubjectDao().getSubjectWithId(userId, sourceFile.getSubjectId());
		String subjectName = subject.getSubjectName() + CheetahWebConstants.FILENAME_PATTERN_SEPARATOR;
		String activityId = entry.getAttribute(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID);
		String filename = subjectName + activityId + ".webm";

		try (FileInputStream input = new FileInputStream(videoOutput)) {
			long trimmedVideo = userFileDao.saveUserFile(userId, filename, "video/webm", input, subject, false);
			userFileDao.addTags(trimmedVideo, "trimmed", "video");
			logSuccessNotification("Created trimmed video " + filename);
		} catch (Exception e) {
			logErrorNotification("Could not save a trimmed video.");
		}

		videoOutput.delete();
	}
}
