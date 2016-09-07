package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.RenameFilesRequest;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.EyeTrackingCache;
import org.cheetahplatform.web.util.FileUtils;

public class RenameFilesServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 9083897016450807057L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		RenameFilesRequest renameRequest = readJson(request, RenameFilesRequest.class);
		UserFileDao userFileDao = new UserFileDao();

		Map<Long, String> toRename = new HashMap<Long, String>();

		for (Entry<Long, String> fileToRename : renameRequest.getFiles().entrySet()) {
			UserFileDto file = userFileDao.getFile(fileToRename.getKey());
			List<UserFileDto> derivedFiles = userFileDao.getDerivedFiles(connection, fileToRename.getKey());
			for (UserFileDto derived : derivedFiles) {
				if (!derived.getFilename().endsWith(".csv")) {
					continue;
				}

				String derivedName = FileUtils.getFileNameWithoutExtension(derived.getFilename());
				String fileName = FileUtils.getFileNameWithoutExtension(file.getFilename());
				if (fileName.equals(derivedName)) {
					String newName = FileUtils.getFileNameWithoutExtension(fileToRename.getValue()) + ".csv";
					toRename.put(derived.getId(), newName);
					EyeTrackingCache.INSTANCE.invalidateCache(derived.getId());
				}
			}
		}
		toRename.putAll(renameRequest.getFiles());
		userFileDao.updateFileName(connection, toRename);
	}
}
