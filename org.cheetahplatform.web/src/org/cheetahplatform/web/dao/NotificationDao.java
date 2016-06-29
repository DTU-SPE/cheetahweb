package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dto.NotificationDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class NotificationDao {
	public static final String NOTIFICATION_SUCCESS = "success";
	public static final String NOTIFICATION_ERROR = "danger";
	public static final String NOTIFICATION_WARNING = "warning";
	public static final String NOTIFICATION_INFO = "info";

	public void clearNotifications(long userId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement("delete from notification where fk_user=?;");
		statement.setLong(1, userId);
		statement.executeUpdate();
		statement.close();
		connection.close();
	}

	public List<NotificationDto> getNotifications(long userId, boolean onlyUnread) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement notificationStatement = null;
		if (onlyUnread) {
			notificationStatement = connection.prepareStatement(
					"select pk_notification, message, type, url, is_read, timestamp from notification where fk_user=? and notification.is_read=?");
			notificationStatement.setLong(1, userId);
			notificationStatement.setBoolean(2, false);
		} else {
			notificationStatement = connection
					.prepareStatement("select pk_notification, message, type, url, is_read, timestamp from notification where fk_user=?");
			notificationStatement.setLong(1, userId);
		}

		List<NotificationDto> notifications = new ArrayList<NotificationDto>();
		ResultSet result = notificationStatement.executeQuery();
		while (result.next()) {
			long id = result.getLong("pk_notification");
			String message = result.getString("message");
			String type = result.getString("type");
			String url = result.getString("url");
			boolean isRead = result.getBoolean("is_read");
			Date timestamp = result.getDate("timestamp");

			notifications.add(new NotificationDto(id, message, type, url, isRead, timestamp));
		}
		result.close();
		notificationStatement.close();
		connection.close();
		return notifications;
	}

	public void insertNotification(Connection connection, String message, String type, long userId) throws SQLException {
		PreparedStatement notificationStatement = connection
				.prepareStatement("insert into notification (fk_user, message, type, timestamp, is_read) values (?,?,?,now(),?)");

		notificationStatement.setLong(1, userId);
		notificationStatement.setString(2, message);
		notificationStatement.setString(3, type);
		notificationStatement.setBoolean(4, false);
		notificationStatement.executeUpdate();
		notificationStatement.close();
	}

	public void insertNotification(String message, String type, long userId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		insertNotification(connection, message, type, userId);
		connection.close();
	}

	public void updateNotification(Connection connection, NotificationDto notification) throws SQLException {
		PreparedStatement updateNotificationStatement = connection
				.prepareStatement("update notification set message=?, type=?, is_read=? where pk_notification=?");

		updateNotificationStatement.setString(1, notification.getMessage());
		updateNotificationStatement.setString(2, notification.getType());
		updateNotificationStatement.setBoolean(3, notification.isRead());
		updateNotificationStatement.setLong(4, notification.getId());
		updateNotificationStatement.executeUpdate();
		updateNotificationStatement.close();
	}
}
