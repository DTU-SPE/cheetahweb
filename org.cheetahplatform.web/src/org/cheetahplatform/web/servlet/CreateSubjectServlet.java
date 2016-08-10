package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dto.CreateSubjectRequest;
import org.cheetahplatform.web.dto.CreateSubjectResponse;

public class CreateSubjectServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 2057615580497961482L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		CreateSubjectRequest createSubjecRequest = readJson(request, CreateSubjectRequest.class);
		SubjectDao subjectDao = new SubjectDao();
		String error = null;

		if (createSubjecRequest.getAllowDouble()) {
			CreateSubjectResponse createSubject = subjectDao.createSubject(connection, createSubjecRequest);
			writeJson(response, createSubject);
		} else {
			if (subjectDao.subjectExists(connection, createSubjecRequest.getEmail())) {
				error = "A user with the email adress \"" + createSubjecRequest.getEmail() + "\" exists already in the database.";
				writeJson(response, new CreateSubjectResponse(error));
			} else {
				CreateSubjectResponse createSubject = subjectDao.createSubject(connection, createSubjecRequest);
				writeJson(response, createSubject);
			}
		}
	}
}
