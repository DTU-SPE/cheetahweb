package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dto.NotificationDto;

import com.fasterxml.jackson.core.JsonParseException;

public class SaveNotificationServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 6761125256822297232L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		try {
			NotificationDto notification = readJson(req, NotificationDto.class);
			new NotificationDao().updateNotification(connection, notification);
		} catch (JsonParseException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
