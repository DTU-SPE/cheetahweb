package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UserFileDto;

public class MyFilesServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 4262925262994307043L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {

		long userid = new UserDao().getUserId(connection, request);
		List<UserFileDto> files = new UserFileDao().getUserFiles(userid);

		writeJson(response, files);
	}
}
