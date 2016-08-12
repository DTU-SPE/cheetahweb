package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DataProcessingDao;

public class DeleteDataProcessingServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -9216762176098047109L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		long toDelete = Long.parseLong(request.getParameter("id"));
		DataProcessingDao dao = new DataProcessingDao();
		dao.delete(connection, toDelete);
	}

}
