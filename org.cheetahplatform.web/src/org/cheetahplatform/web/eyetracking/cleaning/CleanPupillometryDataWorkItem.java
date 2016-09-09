package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.FilterRequest;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.util.FileUtils;

/**
 * Represents a work item for cleaning a pupillometry file.
 *
 * @author stefan.zugal
 *
 */
public class CleanPupillometryDataWorkItem extends AbstractCheetahWorkItem implements IDataProcessingWorkItem {

	public static final String STUDIO_EVENT_DATA = "StudioEventData";
	public static final String STUDIO_EVENT = "StudioEvent";
	public static final String TRIAL_ID = "TrialId";
	/**
	 * The filter request to be processed.
	 */
	private FilterRequest request;

	public CleanPupillometryDataWorkItem(long userId, FilterRequest request, long fileId) {
		super(userId, fileId, "Cleaning pupillometry data");
		this.request = request;
	}

	/**
	 * Analyze data for Thomas Maran.
	 *
	 * @param request
	 *
	 * @param userId
	 * @param originalFileDto
	 * @param contributor
	 *
	 * @param reader
	 * @throws IOException
	 * @throws SQLException
	 */
	private void analyzeData(PupillometryFile file, FilterRequest request, long userId, UserFileDto originalFileDto,
			IAnalysisContributor contributor) throws IOException, SQLException {
		// compute trial column if necessary (Tobii output does not provide the trial id)
		if (!file.hasColumn(TRIAL_ID)) {
			computeTrialColumn(file, contributor);
		}

		PupillometryFile output = new PupillometryFile(request.getDecimalSeparator());
		PupillometryFileColumn eventColumn = output.appendColumn("Interval");
		PupillometryFileColumn trialColumn = output.appendColumn("Trial");
		PupillometryFileColumn blinksColumn = output.appendColumn("Blinks");
		PupillometryFileColumn missingTotalColumn = output.appendColumn("Missing Total");
		PupillometryFileColumn missingPercentColumn = output.appendColumn("Missing Percent");
		PupillometryFileColumn leftPupilAverageColumn = output.appendColumn("200ms Before Event - Left Pupil");
		PupillometryFileColumn rightPupilAverageColumn = output.appendColumn("200ms Before Event - Right Pupil");
		PupillometryFileColumn averagePupilAverageColumn = output.appendColumn("200ms Before Event - Average Pupil");
		PupillometryFileColumn leftRelativeColumn = output.appendColumn("Relative Size - Left Pupil");
		PupillometryFileColumn rightRelativeColumn = output.appendColumn("Relative Size - Right Pupil");
		PupillometryFileColumn averageRelativeColumn = output.appendColumn("Relative Size - Average Pupil");

		// columns for output
		PupillometryFileHeader header = file.getHeader();
		PupillometryFileColumn detectedBlinkColumn = header.getColumn(BlinkDetectionFilter.BLINK_COLUMN);
		PupillometryFileColumn detectedMissingColumn = header.getColumn(SubstitutePupilFilter.MISSING_COLUMN);
		PupillometryFileColumn timestampColumn = header.getColumn(request.getTimestampColumn());
		PupillometryFileColumn leftPupilColumn = header.getColumn(request.getLeftPupilColumn());
		PupillometryFileColumn rightPupilColumn = header.getColumn(request.getRightPupilColumn());
		PupillometryFileColumn trialIdColumn = header.getColumn(TRIAL_ID);
		PupillometryFileColumn sceneColumn = header.getColumn(PupillometryFile.SCENE);
		PupillometryFileColumn relativeTrialTimestampColumn = file.appendColumn("Timestamp (Trial)");
		PupillometryFileColumn relativeEventTimestampColumn = file.appendColumn("Timestamp (Event)");
		PupillometryFileColumn leftRelativePupilColumn = file.appendColumn("Left Pupil (Relative)");
		PupillometryFileColumn rightRelativePupilColumn = file.appendColumn("Right Pupil (Relative)");
		PupillometryFileColumn averageRelativePupilColumn = file.appendColumn("Average Pupil (Relative)");

		LinkedList<PupillometryFileLine> content = file.getContent();
		int blinks = 0;
		boolean isBlink = false;
		int missing = 0;
		int total = 0;
		long trialStartTimestamp = content.getFirst().getLong(timestampColumn);
		long eventStartTimestamp = trialStartTimestamp;
		int steps = 0;
		int trial = -1;
		double averageLeft = -1;
		double averageRight = -1;
		double averageAverage = -1;
		String previousScene = null;
		List<Double> leftRelativePupils = new ArrayList<>();
		List<Double> rightRelativePupils = new ArrayList<>();
		List<Double> averageRelativePupils = new ArrayList<>();

		ListIterator<PupillometryFileLine> iterator = content.listIterator();
		while (iterator.hasNext()) {
			PupillometryFileLine line = iterator.next();

			if (contributor.isSceneStart(file, iterator, line)) {
				int newTrial = line.getInteger(trialIdColumn);
				if (trial != newTrial) {
					trialStartTimestamp = line.getLong(timestampColumn);
				}
				eventStartTimestamp = line.getLong(timestampColumn);

				trial = newTrial;
				blinks = 0;
				missing = 0;
				total = 0;
				steps = 0;
				double sumLeft = 0;
				double sumRight = 0;
				double sumAverage = 0;
				leftRelativePupils.clear();
				rightRelativePupils.clear();
				averageRelativePupils.clear();

				// compute the average pupil size for the last 200ms, skip the current line as it is already the start of a scene
				iterator.previous();
				PupillometryFileLine currentLine = iterator.previous();
				long startTimestamp = Long.parseLong(currentLine.get(timestampColumn));
				long currentTimestamp = Long.parseLong(currentLine.get(timestampColumn));

				while (startTimestamp - currentTimestamp < 200000) {
					steps++;
					double left = currentLine.getDouble(leftPupilColumn);
					sumLeft += left;
					double right = currentLine.getDouble(rightPupilColumn);
					sumRight += right;
					sumAverage += (left + right) / 2;

					currentLine = iterator.previous();
					currentTimestamp = currentLine.getLong(timestampColumn);
				}

				averageLeft = sumLeft / steps;
				averageRight = sumRight / steps;
				averageAverage = sumAverage / steps;

				// reset the cursor, also take care of the initial 2x previous
				for (int i = 0; i < steps + 2; i++) {
					iterator.next();
				}
			}

			if (detectedBlinkColumn != null) {
				boolean newIsBlink = line.get(detectedBlinkColumn).trim().length() > 0;
				if (!isBlink && newIsBlink) { // count only the number of blinks
					blinks++;
				}
				isBlink = newIsBlink;
			}

			boolean isMissing = line.get(detectedMissingColumn).trim().length() > 0;
			if (isMissing) {
				missing++;
			}

			total++;

			long timestamp = line.getLong(timestampColumn);
			long relativeTrialTime = timestamp - trialStartTimestamp;
			line.setValue(relativeTrialTimestampColumn, relativeTrialTime);

			long relativeEventTime = timestamp - eventStartTimestamp;
			line.setValue(relativeEventTimestampColumn, relativeEventTime);

			// add relative pupil sizes; not possible for empty columns (this may happen at the end of a file when no more pupillometry data
			// is delivered)
			if (averageLeft != -1 && !line.isEmpty(leftPupilColumn)) {
				double left = line.getDouble(leftPupilColumn);
				double leftRelative = left / averageLeft;
				line.setValue(leftRelativePupilColumn, leftRelative);
				double right = line.getDouble(rightPupilColumn);
				double rightRelative = right / averageRight;
				line.setValue(rightRelativePupilColumn, rightRelative);
				double average = (left + right) / 2;
				double averageRelativ = average / averageAverage;
				line.setValue(averageRelativePupilColumn, averageRelativ);

				leftRelativePupils.add(leftRelative);
				rightRelativePupils.add(rightRelative);
				averageRelativePupils.add(averageRelativ);
			}

			// collect results if the scene ended
			if (contributor.isSceneEnd(file, iterator, line)) {
				PupillometryFileLine newLine = output.appendLine();

				newLine.setValue(leftPupilAverageColumn, averageLeft);
				newLine.setValue(rightPupilAverageColumn, averageRight);
				newLine.setValue(averagePupilAverageColumn, averageAverage);
				newLine.setValue(trialColumn, trial);
				newLine.setValue(eventColumn, previousScene);
				newLine.setValue(blinksColumn, blinks);
				newLine.setValue(missingTotalColumn, missing);
				newLine.setValue(missingPercentColumn, (double) missing / total);
				newLine.setValue(leftRelativeColumn, average(leftRelativePupils));
				newLine.setValue(rightRelativeColumn, average(rightRelativePupils));
				newLine.setValue(averageRelativeColumn, average(averageRelativePupils));
			}

			// keep the previous scene for processing
			previousScene = line.get(sceneColumn);
		}

		UserFileDao userFileDao = new UserFileDao();
		String fileName = originalFileDto.getFilename();
		int position = fileName.lastIndexOf(".");
		String newName = fileName.substring(0, position) + CheetahWebConstants.FILENAME_PATTERN_SEPARATOR + "results"
				+ fileName.substring(position);
		String relativePath = userFileDao.generateRelativePath(userId, newName);

		String absolutePath = userFileDao.getAbsolutePath(relativePath);
		output.writeToFile(new File(absolutePath));
		userFileDao.insertUserFile(userId, newName, relativePath, "application/octet-stream", "");
	}

	private double average(List<Double> toAverage) {
		double sum = 0;
		for (Double value : toAverage) {
			sum += value;
		}

		return sum / toAverage.size();
	}

	private void computeTrialColumn(PupillometryFile file, IAnalysisContributor contributor) throws IOException {
		PupillometryFileColumn trialColumn = file.appendColumn(TRIAL_ID);
		PupillometryFileHeader header = file.getHeader();
		PupillometryFileColumn studioEventColumn = header.getColumn(STUDIO_EVENT);
		PupillometryFileColumn studioEventDataColumn = header.getColumn(STUDIO_EVENT_DATA);

		contributor.computeTrialColumn(file, trialColumn, studioEventColumn, studioEventDataColumn);
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto originalFileDto = userFileDao.getFile(fileId);

		String filePath = userFileDao.getPath(fileId);
		File file = userFileDao.getUserFile(filePath);
		PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true,
				request.getDecimalSeparator());

		performPreProcessing(originalFileDto, pupillometryFile);

		List<DataProcessingResult> results = processFilters(originalFileDto, pupillometryFile);

		StringBuilder appliedFilters = new StringBuilder();
		for (DataProcessingResult dataProcessingResult : results) {
			// something went wrong - let the user know.
			if (dataProcessingResult.isError()) {
				String errorMessage = dataProcessingResult.getMessage();
				if (dataProcessingResult.getAdditionalInformation() != null) {
					errorMessage = errorMessage + "(" + dataProcessingResult.getAdditionalInformation() + ").";
				}
				logErrorNotification(errorMessage);
				return;
			}

			if (appliedFilters.length() > 0) {
				appliedFilters.append(", ");
			}

			appliedFilters.append(dataProcessingResult.getMessage());
			if (dataProcessingResult.getAdditionalInformation() != null) {
				appliedFilters.append(" (");
				appliedFilters.append(dataProcessingResult.getAdditionalInformation());
				appliedFilters.append(")");
			}
		}

		String newName = null;
		String fileName = originalFileDto.getFilename();
		String subjectName = FileUtils.getSubjectName(fileName, originalFileDto.getSubjectId(), userId);
		if (!subjectName.trim().isEmpty()) {
			subjectName = subjectName + CheetahWebConstants.FILENAME_PATTERN_SEPARATOR;
		}

		String fileNamePostFix = request.getFileNamePostFix();
		if (fileNamePostFix != null && !fileNamePostFix.trim().isEmpty()
				&& fileName.contains(CheetahWebConstants.FILENAME_PATTERN_SEPARATOR)) {

			newName = subjectName + fileNamePostFix + FileUtils.getFileExtension(fileName);
		} else {
			int position = fileName.lastIndexOf(".");
			newName = subjectName + fileName.substring(0, position) + CheetahWebConstants.FILENAME_PATTERN_SEPARATOR + "filtered"
					+ fileName.substring(position);
		}

		String relativePath = userFileDao.generateRelativePath(userId, newName);

		if (request.isAnalyisDefined()) {
			System.out.println("Running analysis...");
			analyzeData(pupillometryFile, request, userId, originalFileDto, request.resolveAnalysisContributor());
			System.out.println("Analysis finished.");
		}

		String absolutePath = userFileDao.getAbsolutePath(relativePath);
		pupillometryFile.writeToFile(new File(absolutePath));
		long cleanedId = userFileDao.insertUserFile(userId, newName, relativePath, originalFileDto.getType(),
				"Applied filters: " + appliedFilters.toString(), null, originalFileDto.getSubjectId(), false, null);
		logSuccessNotification("Pupillometry data cleaned successfully! New file: " + newName);
		userFileDao.addTags(cleanedId, UserFileDao.TAG_CLEANED);
	}

	@Override
	public boolean doWork(PupillometryFile file, DataProcessingContext context) throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto originalFileDto = userFileDao.getFile(fileId);
		List<DataProcessingResult> results = processFilters(originalFileDto, file);
		context.addAllDataProcessingResults(results);
		for (DataProcessingResult dataProcessingResult : results) {
			if (dataProcessingResult.isError()) {
				return false;
			}
		}
		return true;
	}

	private void performPreProcessing(UserFileDto originalFileDto, PupillometryFile pupillometryFile) throws SQLException, IOException {
		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn timestampColumn = header.getColumn(request.getTimestampColumn());
		if (timestampColumn == null) {
			logErrorNotification(
					"Could not find timestamp column '" + request.getTimestampColumn() + "' in file " + originalFileDto.getFilename());
			return;
		}

		PupillometryFileColumn leftPupilColumn = header.getColumn(request.getLeftPupilColumn());
		if (leftPupilColumn == null) {
			logErrorNotification(
					"Could not find left pupil column '" + request.getLeftPupilColumn() + "' in file " + originalFileDto.getFilename());
			return;
		}

		PupillometryFileColumn rightPupilColumn = header.getColumn(request.getRightPupilColumn());
		if (rightPupilColumn == null) {
			logErrorNotification(
					"Could not find right pupil column '" + request.getRightPupilColumn() + "' in file " + originalFileDto.getFilename());
			return;
		}

		// some particular processing for Thomas Maran
		IAnalysisContributor contributor = request.resolveAnalysisContributor();
		if (request.isAnalyisDefined()) {
			pupillometryFile.processSceneColumns(contributor);
			pupillometryFile.copyColumn(leftPupilColumn, leftPupilColumn.getName() + " (raw)");
			pupillometryFile.copyColumn(rightPupilColumn, rightPupilColumn.getName() + " (raw)");
		}

		// do some pre-processing
		pupillometryFile.collapseEmptyColumns(timestampColumn);
		pupillometryFile.removeNullValues("-1");
		pupillometryFile.adaptTimestamps(timestampColumn);
	}

	private List<DataProcessingResult> processFilters(UserFileDto originalFileDto, PupillometryFile pupillometryFile)
			throws IOException, SQLException {
		PupillometryFileHeader header = pupillometryFile.getHeader();
		List<IPupillometryFilter> filters = request.getPupillometryFilters();
		List<DataProcessingResult> results = new ArrayList<>();

		for (IPupillometryFilter filter : filters) {
			for (PupillometryParameter parameter : filter.getDto().getParameters()) {
				String columnName = request.getParameter(parameter.getKey());
				if (parameter.isDataColumn() && !header.hasColumn(columnName)) {
					DataProcessingResult dataProcessingResult = new DataProcessingResult(filter.getName(), true);
					String additionalInfomation = "Could not find the following column: " + columnName;
					dataProcessingResult.setAdditionalInformation(additionalInfomation);
					return results;
				}
			}
		}

		for (IPupillometryFilter filter : filters) {
			DataProcessingResult dataProcessingResult = new DataProcessingResult(filter.getName());
			results.add(dataProcessingResult);
			try {
				String result = filter.run(request, pupillometryFile);
				dataProcessingResult.setAdditionalInformation(result);
			} catch (Exception e) {
				e.printStackTrace();
				String errorMessage = "An error occured when cleaning file: " + originalFileDto.getFilename();
				dataProcessingResult.setAdditionalInformation(errorMessage);
				dataProcessingResult.setError(true);
				return results;
			}
		}
		return results;
	}
}
