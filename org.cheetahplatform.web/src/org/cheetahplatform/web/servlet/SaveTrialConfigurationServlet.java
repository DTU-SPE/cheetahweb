package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DataProcessingDao;
import org.cheetahplatform.web.eyetracking.analysis.TrialConfiguration;

/**
 * Servlet implementation class SaveTrialConfigurationServlet
 */
public class SaveTrialConfigurationServlet extends AbstractCheetahServlet {
	static class SaveTrialConfigurationRequest {
		private long dataProcessingId;
		private TrialConfiguration trialConfiguration;

		public long getDataProcessingId() {
			return dataProcessingId;
		}

		public TrialConfiguration getTrialConfiguration() {
			return trialConfiguration;
		}

		public void setDataProcessingId(long dataProcessingId) {
			this.dataProcessingId = dataProcessingId;
		}

		public void setTrialConfiguration(TrialConfiguration trialConfiguration) {
			this.trialConfiguration = trialConfiguration;
		}

	}

	private static final long serialVersionUID = -5166730230910211537L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		SaveTrialConfigurationRequest saveRequest = readJson(request, SaveTrialConfigurationRequest.class);

		DataProcessingDao dao = new DataProcessingDao();
		TrialConfiguration trialConfiguration = saveRequest.getTrialConfiguration();
		String configurationAsJson = writeJson(trialConfiguration);
		dao.updateTrialConfiguration(connection, saveRequest.getDataProcessingId(), configurationAsJson);
	}
}
