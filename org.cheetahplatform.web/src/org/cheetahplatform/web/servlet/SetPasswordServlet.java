package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.UserCredentialsDto;

import com.fasterxml.jackson.core.JsonParseException;

public class SetPasswordServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 8439208519819051896L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		try {
			UserCredentialsDto credentials = readJson(req, UserCredentialsDto.class);
			UserDao userDao = new UserDao();
			if (userDao.isOldPasswordCorrect(connection, credentials)) {
				userDao.setPassword(connection, credentials);
			} else {
				resp.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			}
		} catch (JsonParseException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
