package org.cheetahplatform.web.servlet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.PhaseLabel;
import org.cheetahplatform.web.dto.SequencesOfPupillometryRequest;
import org.cheetahplatform.web.dto.SequencesOfPupillometryRequestLabeled;
import org.cheetahplatform.web.dto.TimeSlot;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuardMeasures;
import org.cheetahplatform.web.eyetracking.analysis.LabeledPhaseAverageLoadForTsvFileWorkItem;
import org.cheetahplatform.web.eyetracking.analysis.PhaseAverageLoadForTsvFileWorkItem;
import org.cheetahplatform.web.servlet.FileUploadServlet.FileUploadRespone;

/**
 * Servlet implementation class SequencesOfPupillometryServlet
 */
public class SequencesOfPupillometryServletForCSV extends AbstractCheetahServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		long userId = new UserDao().getUserId(connection, req);
		List<FileItem> file = null;
		try {
			file = upload.parseRequest(req);
		} catch (FileUploadException e) {
			throw new ServletException(e);
		}
		if (file == null || file.isEmpty()) {
			return;
		}
		FileItem fileItem = file.get(0);
		InputStream input = fileItem.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		int lineCount = 1;
		try {
			String line = reader.readLine();
			if (!line.trim().equals("Type")) {
				throw new Exception("Line " + lineCount++ + " in your file is not correct.\n The line should be \"Type\" ");
			}
			lineCount++;
			line = reader.readLine();
			if (line.trim().equals("TimeSlots")) {
				lineCount++;
				SequencesOfPupillometryRequest request = makeCreateSubjectReequests(reader, lineCount);
				List<Long> fileIds = request.getFileIds();
				CheetahWorkItemGuard guard = new CheetahWorkItemGuardMeasures(fileIds.size(), userId, "Measures_For_Phases",
						"Calculated Measures For Phases from .tsv files.");
				for (Long fileId : fileIds) {
					PhaseAverageLoadForTsvFileWorkItem analyzer = new PhaseAverageLoadForTsvFileWorkItem(fileId,
							request.getLeftPupilColumn(), request.getRightPupilColumn(), request.getTimeSlots(),
							request.getTimeStampsColumn(), request.getBaseline(), userId, guard);
					CheetahWorker.schedule(analyzer);
				}
			} else if (line.trim().equals("Labeled")) {
				lineCount++;
				SequencesOfPupillometryRequestLabeled request = makeCreateSubjectReequestsLabeld(reader, lineCount);
				List<Long> fileIds = request.getFileIds();
				CheetahWorkItemGuard guard = new CheetahWorkItemGuardMeasures(fileIds.size(), userId, "Measures_For_Phases",
						"Calculated Measures For Phases from .tsv files.");
				for (Long fileId : fileIds) {
					LabeledPhaseAverageLoadForTsvFileWorkItem analyzer = new LabeledPhaseAverageLoadForTsvFileWorkItem(fileId,
							request.getLeftPupilColumn(), request.getRightPupilColumn(), request.getLabelList(), request.getLabelColumn(),
							request.getBaseline(), userId, guard);
					CheetahWorker.schedule(analyzer);
				}

			} else {
				throw new Exception(
						"Line " + lineCount + " in your file is not correct.\n The line should be \"TimeSlots\" or \"Labeled\" ");
			}
		} catch (Exception e) {

			writeJson(resp, new FileUploadRespone(e.getMessage()));
			return;
		}
	}

	private List<Long> extractFileIds(String line, Integer lineCount) throws Exception {
		if ((line.trim().equals(""))) {
			throw new Exception("Line " + lineCount + " in your file is not correct.");
		}
		String[] array = line.split(";");
		ArrayList<Long> fileIds = new ArrayList<>();
		for (String entry : array) {
			try {
				Long fileId = Long.valueOf(entry);
				fileIds.add(fileId);
			} catch (NumberFormatException e) {
				throw new Exception("Line " + lineCount + " in your file is not correct.\n The FileId must be a number.");
			}
		}
		return fileIds;
	}

	private SequencesOfPupillometryRequest makeCreateSubjectReequests(BufferedReader reader, int lineCount) throws Exception {
		ArrayList<TimeSlot> timeSlotList = new ArrayList<>();
		String line = reader.readLine();
		if (!line.trim().equals("Left Pupil")) {
			throw new Exception("Line " + lineCount++ + " in your file is not correct.\n The line should be \"Left Pupil\" ");
		}
		lineCount++;
		line = reader.readLine();
		String pupilLeft = line;
		line = reader.readLine();
		if (!line.trim().equals("Right Pupil")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Right Pupil\" ");
		}
		lineCount++;
		line = reader.readLine();
		String pupilRight = line;
		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("Timestamps")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Timestamps\" ");
		}
		lineCount++;
		line = reader.readLine();
		String timeStampColumn = line;

		List<Long> fileIds = new ArrayList<>();
		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("Files")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Files\" ");
		}
		lineCount++;
		line = reader.readLine();
		fileIds = extractFileIds(line, lineCount);

		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("Baseline")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Baseline\" ");
		}
		lineCount++;
		line = reader.readLine();
		String baseLine = null;
		if (line.equals("null")) {
			baseLine = null;
		} else {
			baseLine = line;
		}
		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("start;end;name")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"start;end;name\".");
		}
		lineCount++;
		line = reader.readLine();

		while (line != null) {
			Long start;
			Long end;
			String name;
			if (!(line.trim().equals(""))) {
				System.out.println(line);
				String[] array = line.split(";");
				if (array.length != 3) {
					throw new Exception("Line " + lineCount + " in your file is not correct.");
				}
				try {
					start = Long.valueOf(array[0]);
				} catch (NumberFormatException e) {
					throw new Exception("Line " + lineCount + " in your file is not correct. ");

				}

				try {
					end = Long.valueOf(array[1]);
				} catch (NumberFormatException e) {
					throw new Exception("Line " + lineCount + " in your file is not correct.");

				}
				if (array[2].trim().equals("")) {
					throw new Exception("Line " + lineCount + " in your file is not correct.");
				} else {
					name = array[2];
				}
				if (end < start) {
					throw new Exception("Line " + lineCount + " in your file is not correct.\n Starttime should be smaller than end.");
				}
				TimeSlot timeSlot = new TimeSlot(start, end, name);
				String consistencyOfTimeSlots = timeSlot.checkConsistency(timeSlotList);
				if (!consistencyOfTimeSlots.equals("")) {
					throw new Exception("Line " + lineCount + " in your file is not correct.\n" + consistencyOfTimeSlots);

				}
				timeSlotList.add(timeSlot);

			}
			lineCount++;
			line = reader.readLine();
		}

		return new SequencesOfPupillometryRequest(fileIds, pupilLeft, pupilRight, timeStampColumn, baseLine, timeSlotList);

	}

	private SequencesOfPupillometryRequestLabeled makeCreateSubjectReequestsLabeld(BufferedReader reader, int lineCount) throws Exception {
		ArrayList<PhaseLabel> phaseList = new ArrayList<>();
		String line = reader.readLine();
		if (!line.trim().equals("Left Pupil")) {
			throw new Exception("Line " + lineCount++ + " in your file is not correct.\n The line should be \"Left Pupil\" ");
		}
		lineCount++;
		line = reader.readLine();
		String pupilLeft = line;
		line = reader.readLine();
		if (!line.trim().equals("Right Pupil")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Right Pupil\" ");
		}
		lineCount++;
		line = reader.readLine();
		String pupilRight = line;
		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("Labels")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Labels\" ");
		}
		lineCount++;
		line = reader.readLine();
		String labelColumn = line;

		List<Long> fileIds = new ArrayList<>();
		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("Files")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Files\" ");
		}
		lineCount++;
		line = reader.readLine();
		fileIds = extractFileIds(line, lineCount);

		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("Baseline")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"Baseline\" ");
		}
		lineCount++;
		line = reader.readLine();
		String baseLine = null;
		if (line.equals("null")) {
			baseLine = null;
		} else {
			baseLine = line;
		}
		lineCount++;
		line = reader.readLine();
		if (!line.trim().equals("labels")) {
			throw new Exception("Line " + lineCount + " in your file is not correct.\n The line should be \"labels\".");
		}
		lineCount++;
		line = reader.readLine();

		while (line != null) {
			String name;
			if (!(line.trim().equals(""))) {
				name = line;
				PhaseLabel phaseLabel = new PhaseLabel(name);
				phaseList.add(phaseLabel);
				lineCount++;
				line = reader.readLine();
			}
		}

		return new SequencesOfPupillometryRequestLabeled(fileIds, pupilLeft, pupilRight, labelColumn, baseLine, phaseList);

	}

}
