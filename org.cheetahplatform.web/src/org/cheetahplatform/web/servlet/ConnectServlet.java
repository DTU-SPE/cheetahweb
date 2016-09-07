package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ConnectRequest;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.AbstractConnectWorkItem;
import org.cheetahplatform.web.eyetracking.ConnectPupillometricDataWorkItem;
import org.cheetahplatform.web.eyetracking.ConnectVideoWorkItem;

public class ConnectServlet extends AbstractCheetahServlet {

	@SuppressWarnings("unused")
	private static class ConnectFileDto {
		private long fileId;
		private long workerId;
		private String filename;

		public ConnectFileDto(long fileId, long workerId, String filename) {
			this.fileId = fileId;
			this.workerId = workerId;
			this.filename = filename;
		}

		public long getFileId() {
			return fileId;
		}

		public String getFilename() {
			return filename;
		}

		public long getWorkerId() {
			return workerId;
		}

	}

	private static final long serialVersionUID = 920834011650831787L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse response)
			throws Exception {
		long userId = new UserDao().getUserId(connection, req);
		ConnectRequest request = readJson(req, ConnectRequest.class);
		UserFileDao userFileDao = new UserFileDao();

		List<Long> files = request.getFiles();
		List<ConnectFileDto> result = new ArrayList<>();
		for (long fileId : files) {
			UserFileDto file = userFileDao.getFile(fileId);
			AbstractConnectWorkItem workItem = null;
			if (file.getType().equals("video/webm")) {
				workItem = new ConnectVideoWorkItem(userId, fileId, request);
			} else {
				workItem = new ConnectPupillometricDataWorkItem(userId, fileId, request);
			}

			CheetahWorker.schedule(workItem);
			result.add(new ConnectFileDto(fileId, workItem.getId(), file.getFilename()));
		}

		writeJson(response, result);
	}
}
