package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.SubjectDao;

public class DeleteSubjectServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 3282334007444084046L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		try {
			@SuppressWarnings("unchecked")
			List<Integer> idList = readJson(req, ArrayList.class);
			SubjectDao subjectDao = new SubjectDao();
			for (Integer id : idList) {
				subjectDao.deleteSubject(connection, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
