package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileUtils;

public class AverageLoadForTsvFileWorkItem extends AbstractCheetahWorkItem {
	private long fileId;
	private String leftPupilColumn;
	private String rightPupilColumn;
	private CheetahWorkItemGuard guard;

	public AverageLoadForTsvFileWorkItem(long fileId, String leftPupilColumn, String rightPupilColumn, long userId,
			CheetahWorkItemGuard guard) {
		super(userId);
		this.fileId = fileId;
		this.leftPupilColumn = leftPupilColumn;
		this.rightPupilColumn = rightPupilColumn;
		this.guard = guard;
	}

	@Override
	public void cancel() {
		guard.canceled(this);
		super.cancel();
	}

	@Override
	public void doWork() throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		File userFile = userFileDao.getUserFile(userFileDao.getPath(fileId));
		PupillometryFile pupillometryFile = new PupillometryFile(userFile, PupillometryFile.SEPARATOR_TABULATOR, true, ".");
		PupillometryFileColumn pupilLeft = pupillometryFile.getHeader().getColumn(leftPupilColumn);
		double[] leftPupil = PupillometryFileUtils.getPupilValues(pupillometryFile, pupilLeft, false);
		double leftMean = new Mean().evaluate(leftPupil);

		PupillometryFileColumn pupilRigt = pupillometryFile.getHeader().getColumn(rightPupilColumn);
		double[] pupilRightValues = PupillometryFileUtils.getPupilValues(pupillometryFile, pupilRigt, false);
		double rightMean = new Mean().evaluate(pupilRightValues);

		List<String> results = new ArrayList<>();
		results.add(String.valueOf(leftMean) + ";" + String.valueOf(rightMean));

		String fileName = userFileDao.getFile(fileId).getFilename();
		String[] splitted = splitFileName(fileName);

		ReportableResult reportableResult = new ReportableResult(splitted[0]);
		DecimalFormat format = new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.GERMAN));
		reportableResult.addResult(splitted[1] + "_" + "pupil_left", format.format(leftMean));
		reportableResult.addResult(splitted[1] + "_" + "pupil_right", format.format(rightMean));
		reportableResult.addResult(splitted[1] + "_" + "source_file_name", fileName);
		guard.reportResult(reportableResult);
	}

	@Override
	public String getDisplayName() {
		return "Calculating Average Load for .tsv file";
	}
}
