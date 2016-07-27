package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserFileDao;

public class DeleteDataServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -173548756290485145L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		long fileId = Long.parseLong(request.getParameter("id"));
		new UserFileDao().deleteFilePermanently(connection, fileId);
	}
}
