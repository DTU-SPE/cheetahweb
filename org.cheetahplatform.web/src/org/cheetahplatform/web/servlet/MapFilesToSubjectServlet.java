package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.MapFilesToSubjectRequest;
import org.cheetahplatform.web.dto.UserFileDto;

public class MapFilesToSubjectServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = -8921169001420785736L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		super.doPostWithDatabaseConnection(connection, request, response);

		UserFileDao userFileDao = new UserFileDao();
		long userId = new UserDao().getUserId(connection, request);

		MapFilesToSubjectRequest data = readJson(request, MapFilesToSubjectRequest.class);
		for (Entry<Long, Long> fileToSubject : data.getFilesToSubjectIds().entrySet()) {
			Long fileId = fileToSubject.getKey();
			Long subjectId = fileToSubject.getValue();

			userFileDao.mapFileToSubject(connection, fileId, subjectId);
			UserFileDto file = userFileDao.getFile(fileId);

			new NotificationDao().insertNotification("Upload successful for file: " + file.getFilename(),
					NotificationDao.NOTIFICATION_SUCCESS, userId);
		}
	}
}
