package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.DataProcessingStepDao;

public class DeleteDataProcessingStepServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 6462106529021296573L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		long toDelete = Long.parseLong(request.getParameter("id"));

		DataProcessingStepDao dao = new DataProcessingStepDao();
		dao.delete(connection, toDelete);
	}
}
