package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UpdateUserDataTagsRequest;

import com.fasterxml.jackson.core.JsonParseException;

public class RemoveUserDataTagsServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = -510711639424390036L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		try {
			UpdateUserDataTagsRequest request = readJson(req, UpdateUserDataTagsRequest.class);
			UserFileDao dao = new UserFileDao();
			dao.removeTags(connection, request.getFileIds(), request.getTags());

		} catch (JsonParseException e) {
			// do not bother
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
