package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dto.ChangeSubjectRequest;

public class ChangeSubjectServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = -3902015630192870956L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ChangeSubjectRequest changeSubjecRequest = readJson(request, ChangeSubjectRequest.class);
		SubjectDao subjectDao = new SubjectDao();
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			subjectDao.changeSubject(connection, changeSubjecRequest);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
