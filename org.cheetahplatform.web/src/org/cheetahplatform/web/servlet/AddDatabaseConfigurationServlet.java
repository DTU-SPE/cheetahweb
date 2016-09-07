package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DatabaseConfigurationDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.DatabaseConfigurationDto;
import org.cheetahplatform.web.dto.ErrorDto;

public class AddDatabaseConfigurationServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 1940861310430059485L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, SQLException {
		try {
			DatabaseConfigurationDto configuration = readJson(request, DatabaseConfigurationDto.class);
			String error = validateDatabase(configuration);
			if (error != null) {
				writeJson(response, new ErrorDto(400, error));
				return;
			}

			UserDao userDao = new UserDao();
			long userId = userDao.getUserId(connection, request);
			DatabaseConfigurationDao dao = new DatabaseConfigurationDao();

			DatabaseConfigurationDto newConfiguration = dao.insertDatabaseConfiguration(connection, userId, configuration.getHost(),
					configuration.getPort(), configuration.getSchema(), configuration.getUsername(), configuration.getPassword());

			writeJson(response, newConfiguration);
		} catch (Exception e) {
			try {
				writeJson(response, new ErrorDto(500, e.getMessage()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private String validateDatabase(DatabaseConfigurationDto configuration) {
		try {
			Properties properties = new Properties();
			properties.put("user", configuration.getUsername());
			properties.put("password", configuration.getPassword());
			properties.put("connectTimeout", "500");

			DriverManager.getConnection(configuration.asMysqlUrl(), properties);
		} catch (SQLException e) {
			return e.getMessage();
		}

		return null;
	}

}
