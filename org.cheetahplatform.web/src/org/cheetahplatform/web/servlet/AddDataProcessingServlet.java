package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DataProcessingDao;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;

public class AddDataProcessingServlet extends AbstractCheetahServlet {
	static class AddDataProcessingRequest {
		private long studyId;
		private String name;
		private String comment;

		public String getComment() {
			return comment;
		}

		public String getName() {
			return name;
		}

		public long getStudyId() {
			return studyId;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setStudyId(long studyId) {
			this.studyId = studyId;
		}

	}

	private static final long serialVersionUID = 3044293481281001070L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		AddDataProcessingRequest addRequest = readJson(request, AddDataProcessingRequest.class);
		DataProcessingDao dao = new DataProcessingDao();
		DataProcessing dataProcessing = dao.insert(connection, addRequest.getStudyId(), addRequest.getName(), addRequest.getComment());
		writeJson(response, dataProcessing);
	}

}
