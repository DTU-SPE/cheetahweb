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
		private String timestampColumn;
		private String leftPupilColumn;
		private String rightPupilColumn;
		private String decimalSeparator;

		public String getComment() {
			return comment;
		}

		public String getDecimalSeparator() {
			return decimalSeparator;
		}

		public String getLeftPupilColumn() {
			return leftPupilColumn;
		}

		public String getName() {
			return name;
		}

		public String getRightPupilColumn() {
			return rightPupilColumn;
		}

		public long getStudyId() {
			return studyId;
		}

		public String getTimestampColumn() {
			return timestampColumn;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public void setDecimalSeparator(String decimalSeparator) {
			this.decimalSeparator = decimalSeparator;
		}

		public void setLeftPupilColumn(String leftPupilColumn) {
			this.leftPupilColumn = leftPupilColumn;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setRightPupilColumn(String rightPupilColumn) {
			this.rightPupilColumn = rightPupilColumn;
		}

		public void setStudyId(long studyId) {
			this.studyId = studyId;
		}

		public void setTimestampColumn(String timestampColumn) {
			this.timestampColumn = timestampColumn;
		}

	}

	private static final long serialVersionUID = 3044293481281001070L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		AddDataProcessingRequest addRequest = readJson(request, AddDataProcessingRequest.class);
		DataProcessingDao dao = new DataProcessingDao();
		DataProcessing dataProcessing = dao.insert(connection, addRequest.getStudyId(), addRequest.getName(), addRequest.getComment(),
				addRequest.getTimestampColumn(), addRequest.getLeftPupilColumn(), addRequest.getRightPupilColumn(),
				addRequest.getDecimalSeparator());

		writeJson(response, dataProcessing);
	}

}
