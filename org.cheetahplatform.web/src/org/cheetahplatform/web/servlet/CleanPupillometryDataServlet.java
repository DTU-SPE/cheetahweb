package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.ICheetahWorkItem;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.FilterRequest;
import org.cheetahplatform.web.eyetracking.cleaning.CleanPupillometryDataWorkItem;

public class CleanPupillometryDataServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 2113500969597546904L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		long userId = new UserDao().getUserId(connection, req);
		FilterRequest request = readJson(req, FilterRequest.class);
		List<Long> files = request.getFiles();
		UserFileDao userFileDao = new UserFileDao();
		Map<Long, String> paths = userFileDao.getPaths(files);

		for (Entry<Long, String> entry : paths.entrySet()) {
			Long fileId = entry.getKey();

			ICheetahWorkItem item = new CleanPupillometryDataWorkItem(userId, request, fileId);
			CheetahWorker.schedule(item);
		}
	}
}
