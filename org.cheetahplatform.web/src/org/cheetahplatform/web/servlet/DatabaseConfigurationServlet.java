package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DatabaseConfigurationDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.DatabaseConfigurationDto;

public class DatabaseConfigurationServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -5680596456547868181L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		UserDao userDao = new UserDao();
		long userId = userDao.getUserId(connection, request);
		DatabaseConfigurationDao dao = new DatabaseConfigurationDao();
		List<DatabaseConfigurationDto> configurations = dao.getDatabaseConfigurations(connection, userId);
		writeJson(response, configurations);
	}
}
