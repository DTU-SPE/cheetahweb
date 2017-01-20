package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.cheetahplatform.common.Activator;
import org.cheetahplatform.web.dao.DataProcessingStepDao;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType;

public class AnalysisStepInserter {
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "XXX";
	private static final String USER = "root";
	private static final String SCHEMA = "cheetah_web";

	public static void main(String[] args) throws SQLException {
		Activator.loadMySQLDriver();

		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);

		DataProcessingStepDao dao = new DataProcessingStepDao();

		List<AnalyzeStepType> all = AnalyzeStepType.ALL;
		for (AnalyzeStepType analyzeStepType : all) {
			if (analyzeStepType.equals(AnalyzeStepType.BLINKS) || analyzeStepType.equals(AnalyzeStepType.MISSING_PERCENT)
					|| analyzeStepType.equals(AnalyzeStepType.MISSING_TOTAL)) {
				continue;
			}

			String configFull = "{\"type\":\"" + analyzeStepType.getId() + "\",\"startTime\":-1,\"endTime\":-1}";
			dao.insert(connection, 6l, "analyze", "full-fixation", configFull);

			String config600_6000 = "{\"type\":\"" + analyzeStepType.getId() + "\",\"startTime\":600,\"endTime\":6000}";
			dao.insert(connection, 6l, "analyze", "600ms-6000ms", config600_6000);

			String config1600_6000 = "{\"type\":\"" + analyzeStepType.getId() + "\",\"startTime\":1600,\"endTime\":6000}";
			dao.insert(connection, 6l, "analyze", "1600ms-6000ms", config1600_6000);

			String config42000_48000 = "{\"type\":\"" + analyzeStepType.getId() + "\",\"startTime\":42000,\"endTime\":48000}";
			dao.insert(connection, 6l, "analyze", "42000ms-48000ms", config42000_48000);

			String config84000_90000 = "{\"type\":\"" + analyzeStepType.getId() + "\",\"startTime\":84000,\"endTime\":90000}";
			dao.insert(connection, 6l, "analyze", "84000ms-90000ms", config84000_90000);
		}
	}

}
