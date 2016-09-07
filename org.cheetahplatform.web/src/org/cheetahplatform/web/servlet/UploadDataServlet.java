package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cheetahplatform.web.dao.NotificationDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.util.FileUtils;

public class UploadDataServlet extends AbstractCheetahServlet {
	private static final int UNABLE_TO_IDENTIFY_SUBJECT = 418;
	private static final long serialVersionUID = -4304859607727283571L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		UserDao userDao = new UserDao();
		long userId = userDao.getUserId(connection, req);
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List<FileItem> files = null;
		try {
			files = upload.parseRequest(req);
		} catch (FileUploadException e) {
			throw new ServletException(e);
		}

		if (files == null || files.isEmpty()) {
			return;
		}

		for (FileItem fileItem : files) {
			String name = fileItem.getName();

			SubjectDto subject = FileUtils.getSubjectForFileName(userId, name);

			try {
				UserFileDao userFileDao = new UserFileDao();
				boolean isHidden = subject == null;
				long userFileId = userFileDao.saveUserFile(userId, fileItem, subject, isHidden);

				if (name.endsWith("tsv")) {
					userFileDao.addTags(userFileId, UserFileDao.TAG_RAW_DATA);
				} else if (name.endsWith("webm") || name.endsWith("mp4")) {
					userFileDao.addTags(userFileId, UserFileDao.TAG_VIDEO);
				}

				UserFileDto userFile = userFileDao.getFile(userFileId);
				if (subject == null) {
					resp.setStatus(UNABLE_TO_IDENTIFY_SUBJECT);
				}

				writeJson(resp, userFile);
			} catch (Exception e) {
				e.printStackTrace();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				new NotificationDao().insertNotification("Upload failed for file:" + name, NotificationDao.NOTIFICATION_ERROR, userId);
				continue;
			} finally {
				fileItem.delete();
			}

			// only display notification if subject was mapped automatically. Otherwise, the user needs to map the file first
			if (subject != null) {
				new NotificationDao().insertNotification("Upload successful for file: " + name, NotificationDao.NOTIFICATION_SUCCESS,
						userId);
			}
		}
	}
}
