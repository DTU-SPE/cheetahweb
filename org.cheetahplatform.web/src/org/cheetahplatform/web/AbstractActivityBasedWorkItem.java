package org.cheetahplatform.web;

import static org.cheetahplatform.common.CommonConstants.ATTRIBUTE_PROCESS_INSTANCE;
import static org.cheetahplatform.modeler.ModelerConstants.ATTRIBUTE_EXPERIMENT_ACTIVITY_DURATION;
import static org.cheetahplatform.modeler.ModelerConstants.ATTRIBUTE_EXPERIMENT_ACTIVITY_ID;
import static org.cheetahplatform.modeler.experiment.ExperimentWorkflowEngine.WORKFLOW_CONFIGURATION_ID;
import static org.cheetahplatform.web.servlet.AbstractCheetahServlet.closeQuietly;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cheetahplatform.common.CommonConstants;
import org.cheetahplatform.common.eyetracking.EyeTrackerDateCorrection;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.common.logging.DatabaseAuditTrailEntry;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.modeler.ExperimentProvider;
import org.cheetahplatform.modeler.experiment.IExperiment;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.CodeAndExperimentActivity;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

/**
 * Base class for work items that work on experiment activity selections, e.g., trimming a file based on an experiment workflow.
 *
 * @author stefan.zugal
 *
 */
public abstract class AbstractActivityBasedWorkItem extends AbstractCheetahWorkItem {

	/**
	 * The experiment activities to be considered when trimming.
	 */
	private List<CodeAndExperimentActivity> activities;
	/**
	 * The name of the column in which the timestamp can be found.
	 */
	private String timestampColumnName;

	public AbstractActivityBasedWorkItem(long userId, long fileId, List<CodeAndExperimentActivity> activities, String timestampColumn,
			String message) {
		super(userId, fileId, message);

		this.activities = activities;
		this.timestampColumnName = timestampColumn;
	}

	protected Date computeTimestamp(PupillometryFileColumn timestampColumn, PupillometryFileColumn localTimestampColumn,
			List<PupillometryFileLine> content, int index) throws ParseException {
		IPupillometryFileLine pupillometryFileLine = content.get(index);
		String timestamp = pupillometryFileLine.get(timestampColumn);
		long parsedTimestamp = Long.parseLong(timestamp) / 1000;
		String localTimestamp = pupillometryFileLine.get(localTimestampColumn);
		return EyeTrackerDateCorrection.correctDate(localTimestamp, parsedTimestamp);
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto file = userFileDao.getFile(fileId);
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PupillometryFile reader = loadPupillometryFile(connection);
		if (reader == null) {
			return;
		}

		PupillometryFileHeader header = reader.getHeader();
		PupillometryFileColumn timestampColumn = header.getColumn(this.timestampColumnName);
		if (timestampColumn == null) {
			logErrorNotification("Could not find timestamp column '" + this.timestampColumnName + "' in file " + file.getFilename());
			return;
		}

		PupillometryFileColumn localTimestampColumn = header.getColumn(LOCAL_TIMESTAMP_COLUMN_HEADER);
		if (localTimestampColumn == null) {
			logErrorNotification(
					"Could not find local timestamp column '" + LOCAL_TIMESTAMP_COLUMN_HEADER + "' in file " + file.getFilename());
			return;
		}

		String fileName = file.getFilename();
		String[] splittedFilename = splitFileName(fileName);
		if (splittedFilename == null) {
			return;
		}

		String subjectName = splittedFilename[0];
		String experimentId = splittedFilename[1];
		IExperiment experiment = ExperimentProvider.getExperiment(experimentId);
		if (experiment == null) {
			logErrorNotification("Could not find experiment " + experimentId
					+ ". Please make sure you have included the experiment's jar file in WEB-INF/lib.");
			return;
		}

		// query for all process instances of the selected subject and experiment
		String query = "SELECT process_instance.database_id FROM subject, process_instance, process WHERE subject.subject_id = ? AND process.id = ? AND process.database_id = process_instance.process AND process_instance.fk_subject = subject.pk_subject;";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, subjectName);
		statement.setString(2, experimentId);
		ResultSet resultSet = statement.executeQuery();

		List<Long> processInstanceIds = new ArrayList<>();
		while (resultSet.next()) {
			long id = resultSet.getLong(1);
			processInstanceIds.add(id);
		}

		if (processInstanceIds.isEmpty()) {
			logErrorNotification("Could not find process instances for subject " + subjectName + " and experiment " + experimentId);
			return;
		}

		// find all experiment activities that were conducted during the eyetracking session
		List<PupillometryFileLine> content = reader.getContent();
		Date sessionStart = computeTimestamp(timestampColumn, localTimestampColumn, content, 0);
		Date sessionEnd = computeTimestamp(timestampColumn, localTimestampColumn, content, content.size() - 1);
		for (Long id : processInstanceIds) {
			ProcessInstance processInstance = DatabasePromReader.readProcessInstance(id, connection);
			long workflowCode = processInstance.getLongAttribute(WORKFLOW_CONFIGURATION_ID);

			for (AuditTrailEntry entry : processInstance.getEntries()) {
				if (!entry.isAttributeDefined(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID)) {
					String message = "Attribute " + ATTRIBUTE_EXPERIMENT_ACTIVITY_ID + " not defined for: " + id + ", experiment "
							+ experimentId + ", entry " + ((DatabaseAuditTrailEntry) entry).getId();
					logErrorNotification(message);
					continue;
				}

				String activityId = entry.getAttribute(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID);
				if (!isActivitySelectedForTrimming(workflowCode, activityId)) {
					continue;
				}

				Date activityEnd = entry.getTimestamp();
				if (!entry.isAttributeDefined(ATTRIBUTE_EXPERIMENT_ACTIVITY_DURATION)) {
					String message = "Attribute " + ATTRIBUTE_EXPERIMENT_ACTIVITY_DURATION + " not defined for: " + id + ", experiment "
							+ experimentId + ", entry " + ((DatabaseAuditTrailEntry) entry).getId();
					logErrorNotification(message);
					continue;
				}

				long duration = entry.getLongAttribute(ATTRIBUTE_EXPERIMENT_ACTIVITY_DURATION);
				Date activityStart = new Date(activityEnd.getTime() - duration);
				boolean isModelingActivity = entry.isAttributeDefined(CommonConstants.ATTRIBUTE_NOTATION);

				// if it is a modeling activity, use the more precise information from the process instance
				if (isModelingActivity) {
					String ppmProcessInstanceId = entry.getAttribute(ATTRIBUTE_PROCESS_INSTANCE);
					long ppmInstanceDatabaseId = DatabasePromReader.getProcessInstanceDatabaseId(ppmProcessInstanceId, connection);
					ProcessInstance ppmInstance = DatabasePromReader.readProcessInstance(ppmInstanceDatabaseId, connection);
					List<AuditTrailEntry> entries = ppmInstance.getEntries();
					if (entries == null || entries.isEmpty()) {
						logErrorNotification("The process instance with id " + ppmProcessInstanceId + " has no audittrail entries.");
						return;
					}

					activityStart = entries.get(0).getTimestamp();
					AuditTrailEntry lastEntry = entries.get(entries.size() - 1);
					activityEnd = lastEntry.getTimestamp();
				}

				if (activityStart.before(sessionEnd) && activityEnd.after(sessionStart)) {
					processExperimentActivity(subjectName, file, reader, content, timestampColumn, localTimestampColumn, entry,
							activityStart, activityEnd, connection);
				}
			}
		}

		closeQuietly(connection);
	}

	/**
	 * Determines whether the given workflow code and activity have been selected for trimming.
	 *
	 * @param code
	 * @param activity
	 * @return
	 */
	private boolean isActivitySelectedForTrimming(long code, String activity) {
		for (CodeAndExperimentActivity codeAndExperimentActivity : activities) {
			long expectedCode = codeAndExperimentActivity.getCode();
			String expectedActivity = codeAndExperimentActivity.getActivity();
			if (expectedCode == code && expectedActivity.equals(activity)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Loads the file to be processed from the disk.
	 *
	 * @return
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	protected PupillometryFile loadPupillometryFile(Connection connection) throws SQLException, FileNotFoundException {
		UserFileDao userFileDao = new UserFileDao();
		File userFile = userFileDao.getUserFile(userFileDao.getPath(fileId));
		return new PupillometryFile(userFile, PupillometryFile.SEPARATOR_TABULATOR, true, ".");
	}

	/**
	 * Carries out work for an experiment activity that was detected to be executed during the eyetracking session.
	 *
	 * @param subjectName
	 *            the name of the subject being processed
	 * @param fileDto
	 *            the file being processed
	 * @param file
	 *            the file being processed
	 * @param content
	 *            the content to be processed
	 * @param timestampColumn
	 *            the column containing the timestamp
	 * @param localTimestampColumn
	 *            the column containing the local timestamp
	 * @param entry
	 *            the entry representing the experiment activity that was executed during the eyetracking session
	 * @param activityStart
	 *            the timepoint when the respective activity started
	 * @param activityEnd
	 *            the timepoint when the respective activity ended
	 * @param connection
	 *            the database connection to be used
	 */
	protected abstract void processExperimentActivity(String subjectName, UserFileDto fileDto, PupillometryFile file,
			List<PupillometryFileLine> content, PupillometryFileColumn timestampColumn, PupillometryFileColumn localTimestampColumn,
			AuditTrailEntry entry, Date activityStart, Date activityEnd, Connection connection) throws Exception;
}
