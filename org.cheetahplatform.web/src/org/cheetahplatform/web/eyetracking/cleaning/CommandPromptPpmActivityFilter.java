package org.cheetahplatform.web.eyetracking.cleaning;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.cheetahplatform.common.eyetracking.EyeTrackerDateCorrection;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.modeler.bpmn.descriptor.BpmnDescriptors;
import org.cheetahplatform.modeler.command.AbstractGraphCommand;
import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.PpmInstanceDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.FilterRequest;
import org.cheetahplatform.web.dto.PpmInstanceDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

/**
 * A PPM specific filter that removes all pupillometric data PPM interactions that where command prompts are opened.
 *
 * @author Jakob
 */
public class CommandPromptPpmActivityFilter extends AbstractPupillometryFilter {

	private class TimeFrame {
		private long start;
		private long end;

		public TimeFrame(long start, long end) {
			this.start = start;
			this.end = end;
		}

		public boolean isInTimeFrame(long timestamp) {
			return start <= timestamp && end >= timestamp;
		}
	}

	private static final String PPM_COMMAND_PROMPT_MARKING = "PPM_COMMAND_PROMPT";

	private static final String MODELING_TASK_PARAMETER = "ppm_command_prompt_modeling_task";

	public CommandPromptPpmActivityFilter(long id) {
		super(id, "Remove pupillometry data during PPM interactions with command prompts.");
	}

	@Override
	protected List<PupillometryParameter> getParameters() {
		List<PupillometryParameter> parameters = super.getParameters();
		parameters.add(new PupillometryParameter(MODELING_TASK_PARAMETER, "The task that should be analyzed", "task_modeling_1.0", false));
		return parameters;
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file, long fileId) throws Exception {
		String modelingTask = request.getParameter(MODELING_TASK_PARAMETER);

		UserFileDto userFile = new UserFileDao().getFile(fileId);
		Long subjectId = userFile.getSubjectId();

		if (subjectId == null) {
			return "Subject id of file not set. This might be a bug. Skipping command prompt filter.";
		}

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		List<PpmInstanceDto> processInstances = new PpmInstanceDao().selectProcessInstancesForSubjectAndTask(connection, subjectId,
				modelingTask);

		if (processInstances == null || processInstances.isEmpty()) {
			connection.close();
			return "Found no PPM instance. Skipping command prompt filter.";
		}

		if (processInstances.size() > 1) {
			connection.close();
			return "Found multiple PPM instances. Skipping command prompt filter.";
		}

		PpmInstanceDto ppmInstanceDto = processInstances.get(0);

		List<TimeFrame> timeFramesToRemove = new ArrayList<>();

		ProcessInstance instance = DatabasePromReader.readProcessInstance(ppmInstanceDto.getProcessInstanceId(), connection);
		List<AuditTrailEntry> entries = instance.getEntries();
		for (AuditTrailEntry auditTrailEntry : entries) {
			long end = auditTrailEntry.getTimestamp().getTime();

			if (AbstractGraphCommand.CREATE_NODE.equals(auditTrailEntry.getEventType())) {
				if (BpmnDescriptors.ACTIVITY.equals(auditTrailEntry.getAttribute(AbstractGraphCommand.DESCRIPTOR))) {
					long start = Long.parseLong(auditTrailEntry.getAttribute(AbstractGraphCommand.ADD_NODE_START_TIME));
					timeFramesToRemove.add(new TimeFrame(start, end));
				}
			} else if (AbstractGraphCommand.RENAME.equals(auditTrailEntry.getEventType())) {
				// when condition of a edge is removed no prompt is opened
				if (auditTrailEntry.isAttributeDefined(AbstractGraphCommand.RENAME_START_TIME)) {
					long start = Long.parseLong(auditTrailEntry.getAttribute(AbstractGraphCommand.RENAME_START_TIME));

					timeFramesToRemove.add(new TimeFrame(start, end));
				}
			}
		}

		long removedLineCount = 0;

		PupillometryFileColumn localTimestampColumn = file.getColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_LOCAL_TIMESTAMP);
		PupillometryFileHeader header = file.getHeader();
		PupillometryFileColumn timestampColumn = getTimestampColumn(request, header);
		PupillometryFileColumn leftPupilColumn = getLeftPupilColumn(request, header);
		PupillometryFileColumn rightPupilColumn = getRightPupilColumn(request, header);

		LinkedList<PupillometryFileLine> content = file.getContent();
		for (PupillometryFileLine pupillometryFileLine : content) {
			Long eyeTrackerTimestamp = pupillometryFileLine.getLong(timestampColumn);
			String localTimestamp = pupillometryFileLine.get(localTimestampColumn);

			Long removeNanos = eyeTrackerTimestamp = eyeTrackerTimestamp / 1000;
			Date correctDate = EyeTrackerDateCorrection.correctDate(localTimestamp, removeNanos);
			long correctTimeStamp = correctDate.getTime();

			for (TimeFrame timeFrame : timeFramesToRemove) {
				if (timeFrame.isInTimeFrame(correctTimeStamp)) {
					pupillometryFileLine.deleteValue(leftPupilColumn);
					pupillometryFileLine.deleteValue(rightPupilColumn);
					pupillometryFileLine.mark(PPM_COMMAND_PROMPT_MARKING);
					removedLineCount++;
				}
			}
		}

		connection.close();

		return "Removed " + removedLineCount + " lines in " + timeFramesToRemove.size() + " PPM interactions with command prompt.";
	}
}
