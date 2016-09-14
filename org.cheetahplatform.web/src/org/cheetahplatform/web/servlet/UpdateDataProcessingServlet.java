package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DataProcessingDao;

/**
 * Servlet implementation class UpdateDataProcessingServlet
 */
public class UpdateDataProcessingServlet extends AbstractCheetahServlet {

	static class UpdateDataProcessingRequest {
		private long dataProcessingId;
		private String timestampColumn;
		private String leftPupilColumn;
		private String rightPupilColumn;
		private String decimalSeparator;

		public long getDataProcessingId() {
			return dataProcessingId;
		}

		public String getDecimalSeparator() {
			return decimalSeparator;
		}

		public String getLeftPupilColumn() {
			return leftPupilColumn;
		}

		public String getRightPupilColumn() {
			return rightPupilColumn;
		}

		public String getTimestampColumn() {
			return timestampColumn;
		}

		public void setDataProcessingId(long dataProcessingId) {
			this.dataProcessingId = dataProcessingId;
		}

		public void setDecimalSeparator(String decimalSeparator) {
			this.decimalSeparator = decimalSeparator;
		}

		public void setLeftPupilColumn(String leftPupilColumn) {
			this.leftPupilColumn = leftPupilColumn;
		}

		public void setRightPupilColumn(String rightPupilColumn) {
			this.rightPupilColumn = rightPupilColumn;
		}

		public void setTimestampColumn(String timestampColumn) {
			this.timestampColumn = timestampColumn;
		}

	}

	private static final long serialVersionUID = 9012654699568730594L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		UpdateDataProcessingRequest updateRequest = readJson(request, UpdateDataProcessingRequest.class);
		DataProcessingDao dao = new DataProcessingDao();
		dao.update(connection, updateRequest.getDataProcessingId(), updateRequest.getTimestampColumn(), updateRequest.getLeftPupilColumn(),
				updateRequest.getRightPupilColumn(), updateRequest.getDecimalSeparator());
	}

}
