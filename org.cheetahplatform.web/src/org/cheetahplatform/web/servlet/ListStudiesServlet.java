package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.StudyDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.StudyDto;

public class ListStudiesServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 7132015388328079547L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		StudyDao studyDao = new StudyDao();
		long userId = new UserDao().getUserId(connection, request);
		List<StudyDto> subjects = studyDao.getStudiesForUser(connection, userId);
		writeJson(response, subjects);
	}
}
