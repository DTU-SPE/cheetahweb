package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.EyeTrackingCache;

public class DeleteUserFileServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 7473652710305102087L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		@SuppressWarnings("unchecked")
		List<Number> filesToDelete = readJson(req, ArrayList.class);
		List<Long> fileIds = new ArrayList<Long>();
		for (Number integer : filesToDelete) {
			fileIds.add(integer.longValue());
		}
		UserFileDao userFileDao = new UserFileDao();
		long userId = new UserDao().getUserId(connection, req);
		Map<Long, Long> ppmInstanceMap = userFileDao.getPpmInstancesForFiles(userId, fileIds);
		for (Entry<Long, Long> entry : ppmInstanceMap.entrySet()) {
			EyeTrackingCache.INSTANCE.invalidateCache(entry.getValue());
		}

		userFileDao.deleteFiles(fileIds, userId);
		List<UserFileDto> userFiles = userFileDao.getUserFiles(userId);
		List<UserFileDto> files = userFiles;
		writeJson(resp, files);
	}
}
