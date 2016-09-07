package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.UserDto;

public class UserDataServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 4650568935084525130L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		UserDao userDao = new UserDao();
		long userId = userDao.getUserId(connection, request);
		UserDto user = userDao.getUserInformation(connection, userId);
		writeJson(response, user);
	}
}
