package org.cheetahplatform.web.util;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

/**
 * Runnable for (re)moving user files that are no longer references in the database, #504.
 *
 * @author stefan.zugal
 *
 */
public class UserfileCleanup {

	private void clean(Connection connection, String query, String pathToClean) throws SQLException {
		ResultSet result = connection.createStatement().executeQuery(query);
		Set<String> allPaths = new HashSet<>();
		while (result.next()) {
			String path = result.getString(1);
			allPaths.add(path);
		}

		File folderToClean = UserFileDao.getPath(pathToClean);
		File archiveFolder = UserFileDao.getPath(UserFileDao.ARCHIVE_PATH);

		for (File folderForUser : folderToClean.listFiles()) {
			if (!folderForUser.isDirectory()) {
				continue;
			}

			for (File file : folderForUser.listFiles()) {
				String path = file.getAbsolutePath();
				path = path.replaceAll("\\\\", "/");
				int startIndex = path.indexOf(pathToClean);
				String relativePath = path.substring(startIndex);

				if (!allPaths.contains(relativePath)) {
					File targetPath = new File(archiveFolder, file.getName());
					file.renameTo(targetPath);
				}
			}
		}
	}

	/**
	 * Cleans up non-referenced user data files and videos. These files are intentionally not immediately deleted to give the user some time
	 * to restore files, if deleted inadvertently.
	 */
	public void cleanUp() {
		try (Connection connection = AbstractCheetahServlet.getDatabaseConnection()) {
			clean(connection, "select path from user_data", UserFileDao.USER_UPLOAD_PATH);
			clean(connection, "select movie_path from eyetracking_movie", UserFileDao.VIDEO_PATH);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
