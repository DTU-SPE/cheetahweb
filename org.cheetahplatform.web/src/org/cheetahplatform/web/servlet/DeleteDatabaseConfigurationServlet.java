package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DatabaseConfigurationDao;

/**
 * Servlet implementation class DeleteDatabaseConfigurationServlet
 */
public class DeleteDatabaseConfigurationServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = -8518941777692307398L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		int toDelete = Integer.parseInt(request.getParameter("configurationId").toString());

		new DatabaseConfigurationDao().deleteConfiguration(connection, toDelete);
	}

}
