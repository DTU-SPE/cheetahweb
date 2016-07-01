package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dao.UserFileDao;

public class DeleteSubjectServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 3282334007444084046L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		try {
			@SuppressWarnings("unchecked")
			List<Integer> idList = readJson(req, ArrayList.class);
			SubjectDao subjectDao = new SubjectDao();
			UserFileDao userFileDao = new UserFileDao();
			List<Integer> hasFile = new ArrayList<>();
			for (Integer id : idList) {
				Integer userFilesForSubject = userFileDao.getUsageOfForeignKeyOfSubject(connection, id);
				if (userFilesForSubject > 0) {
					hasFile.add(id);
				}
			}
			if (hasFile.isEmpty()) {
				for (Integer id : idList) {
					subjectDao.deleteSubject(connection, id);
					writeJson(resp, null);
				}
			} else {
				writeJson(resp, hasFile);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
