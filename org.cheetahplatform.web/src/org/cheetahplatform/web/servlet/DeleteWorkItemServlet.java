package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dto.CheetahWorkItemDto;
import org.cheetahplatform.web.dto.DeleteWorkItemRequest;

public class DeleteWorkItemServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 1310829029749387518L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		long userId = getUserId(connection, req);
		NotificationDao notificationDao = new NotificationDao();
		DeleteWorkItemRequest deleteRequest = readJson(req, DeleteWorkItemRequest.class);

		for (CheetahWorkItemDto workItem : deleteRequest.getWorkItems()) {
			if (!CheetahWorker.cancel(userId, workItem)) {
				notificationDao.insertNotification(
						"Could not remove the following work item: '" + workItem.getMessage()
								+ "'. You either don't have the priviledges to cancel the work item or the work item has been started already.",
						NotificationDao.NOTIFICATION_ERROR, userId);
				return;
			}
		}
	}
}
