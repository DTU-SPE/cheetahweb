package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.dao.UserFileDao;

public class EyetrackingAnalysisWorkItem extends AbstractCheetahWorkItem {
	private List<IEyeTrackingDataAnalyzer> analyzers;

	public EyetrackingAnalysisWorkItem(long userId, List<IEyeTrackingDataAnalyzer> analyzers) {
		super(userId, "Running Eye Tracking Analysis");
		this.analyzers = analyzers;
	}

	@Override
	public void doWork() throws Exception {
		StringBuilder builder = new StringBuilder();
		if (analyzers == null || analyzers.isEmpty()) {
			logErrorNotification("No analyzers found. Nothing to do...");
			return;
		}

		builder.append("Subject;");
		// assuming that all analyzers have the same headers.
		builder.append(analyzers.get(0).getHeader());
		builder.append(";\n");
		for (IEyeTrackingDataAnalyzer analyzer : analyzers) {
			String subject = analyzer.getSubjectIdentifier();
			List<String> result = analyzer.analyze();
			for (String resultLine : result) {
				builder.append(subject);
				builder.append(";");
				builder.append(resultLine);
				builder.append(";");
				builder.append("\n");
			}
		}

		UserFileDao userFileDao = new UserFileDao();
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd_HH-mm");
		String fileName = analyzers.get(0).getName().replaceAll(" ", "_") + "_" + format.format(new java.util.Date()) + ".csv";
		String relativePath = userFileDao.generateRelativePath(userId, fileName);
		String absolutePath = userFileDao.getAbsolutePath(relativePath);
		File file = new File(absolutePath);
		FileWriter writer = new FileWriter(file);
		writer.write(builder.toString());
		writer.close();

		userFileDao.insertUserFile(userId, fileName, relativePath, "application/octet-stream",
				"Calculated " + analyzers.get(0).getName() + ".", null, null, false, null);

		logSuccessNotification("Analysis complete. Find the file '" + fileName + "' in your data section!");
	}

	@Override
	public String getDisplayName() {
		return message;
	}
}
