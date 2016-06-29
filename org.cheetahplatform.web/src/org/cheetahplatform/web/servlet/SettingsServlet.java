package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.SettingsDao;

public class SettingsServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 7399488236997770098L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		super.doGetWithDatabaseConnection(connection, request, response);

		Map<String, String> allSettings = new SettingsDao().getAllSettings(connection);
		writeJson(response, allSettings);
	}
}
