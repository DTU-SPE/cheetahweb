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

public class NotificationServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 3302461998123313144L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {

		long userId = new UserDao().getUserId(connection, request);
		boolean onlyUnread = Boolean.parseBoolean(request.getParameter("onlyUnread"));
		List<NotificationDto> notifications = new NotificationDao().getNotifications(userId, onlyUnread);
		writeJson(response, notifications);
	}
}
