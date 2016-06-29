package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 4795843219084423367L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {

		request.getSession().invalidate();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}
}
