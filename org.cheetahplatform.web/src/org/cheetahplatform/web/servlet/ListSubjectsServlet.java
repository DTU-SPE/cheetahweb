package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.SubjectForSearchDto;

public class ListSubjectsServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -8641665742390883328L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		SubjectDao subjectDao = new SubjectDao();
		long userId = new UserDao().getUserId(connection, request);
		List<SubjectForSearchDto> subjects = subjectDao.getAllSubjectsForUser(connection, userId);
		writeJson(response, subjects);
	}
}
