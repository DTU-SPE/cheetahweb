package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.NotificationDto;

public class ClearNotificationServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -7204597990877262322L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		super.doGetWithDatabaseConnection(connection, request, response);

		long userId = new UserDao().getUserId(connection, request);
		NotificationDao notificationDao = new NotificationDao();
		notificationDao.clearNotifications(userId);

		List<NotificationDto> notifications = notificationDao.getNotifications(userId, false);
		writeJson(response, notifications);
	}
}
