package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lists all *.tsv files that are associated with a given study.
 *
 * @author stefan.zugal
 *
 */
public class ListAvailablePupillometryFilesServlet extends AbstractCheetahServlet {

	static class AvaiblePupillometryFile {
		private long id;
		private String name;

		public AvaiblePupillometryFile(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	static class ListAvailablePupillometryFilesRequest {
		private long studyId;

		public long getStudyId() {
			return studyId;
		}

		public void setStudyId(long studyId) {
			this.studyId = studyId;
		}

	}

	private static final long serialVersionUID = 605369807033436474L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ListAvailablePupillometryFilesRequest listRequest = readJson(request, ListAvailablePupillometryFilesRequest.class);

		PreparedStatement statement = connection.prepareStatement(
				"select pk_user_data, filename from user_data where fk_subject in (select pk_subject from subject where fk_study = ?) and filename like '%.tsv' and hidden = 0;");
		statement.setLong(1, listRequest.getStudyId());
		ResultSet result = statement.executeQuery();
		List<AvaiblePupillometryFile> availableFiles = new ArrayList<>();

		while (result.next()) {
			long id = result.getLong("pk_user_data");
			String name = result.getString("filename");

			availableFiles.add(new AvaiblePupillometryFile(id, name));
		}

		writeJson(response, availableFiles);
	}

}
