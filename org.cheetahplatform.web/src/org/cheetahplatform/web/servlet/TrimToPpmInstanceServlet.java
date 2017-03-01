package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.ICheetahWorkItem;
import org.cheetahplatform.web.TrimVideoWorkItem;
import org.cheetahplatform.web.TrimWorkItem;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.TrimToPpmInstanceRequest;
import org.cheetahplatform.web.dto.UserFileDto;

import com.fasterxml.jackson.core.JsonParseException;

public class TrimToPpmInstanceServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 920834011650831787L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		try {
			long userId = new UserDao().getUserId(connection, req);
			TrimToPpmInstanceRequest request = readJson(req, TrimToPpmInstanceRequest.class);
			List<Long> files = request.getFiles();
			UserFileDao userFileDao = new UserFileDao();

			for (Long fileId : files) {
				UserFileDto file = userFileDao.getFile(fileId);
				String filename = file.getFilename();
				if (filename.endsWith("tsv")) {
					ICheetahWorkItem workItem = new TrimWorkItem(userId, fileId, request.getTimestampColumn(), request.getActivities());
					CheetahWorker.schedule(workItem);
				} else if (filename.endsWith("webm")) {
					TrimVideoWorkItem workItem = new TrimVideoWorkItem(userId, fileId, request.getActivities(),
							request.getTimestampColumn());
					CheetahWorker.schedule(workItem);
				} else {
					NotificationDao notificationDao = new NotificationDao();
					notificationDao.insertNotification(
							"Cannot handle the following file: " + filename
									+ ". The file should be \"*.tsv\" or \"*.wbm\", please convert the file.",
							NotificationDao.NOTIFICATION_ERROR, userId);
				}
			}
		} catch (JsonParseException e) {
			// do not bother
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
