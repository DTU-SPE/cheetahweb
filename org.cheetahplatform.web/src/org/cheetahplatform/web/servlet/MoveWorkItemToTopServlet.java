package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dto.CheetahWorkItemDto;
import org.cheetahplatform.web.dto.MoveWorkItemToTopRequest;

public class MoveWorkItemToTopServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 256576297360399460L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		NotificationDao notificationDao = new NotificationDao();
		long userId = getUserId(connection, req);

		MoveWorkItemToTopRequest request = readJson(req, MoveWorkItemToTopRequest.class);
		List<CheetahWorkItemDto> workItems = request.getWorkItems();
		for (int i = workItems.size() - 1; i >= 0; i--) {
			CheetahWorkItemDto cheetahWorkItemDto = workItems.get(i);
			if (!CheetahWorker.moveToTop(userId, cheetahWorkItemDto)) {
				String message = "Could not move work item '" + cheetahWorkItemDto.getMessage()
						+ "' to top of work queue. You might not have sufficient privileges or the work item has been started in the mean time.";
				notificationDao.insertNotification(message, NotificationDao.NOTIFICATION_ERROR, userId);
			}
		}
	}
}
