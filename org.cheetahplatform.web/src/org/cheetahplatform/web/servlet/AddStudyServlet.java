package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.StudyDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.AddStudyRequest;
import org.cheetahplatform.web.dto.StudyDto;

public class AddStudyServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 654397525385583802L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		StudyDao studyDao = new StudyDao();
		long userId = new UserDao().getUserId(connection, request);

		AddStudyRequest addStudyRequest = readJson(request, AddStudyRequest.class);
		studyDao.insertStudy(connection, userId, addStudyRequest.getName(), addStudyRequest.getComment());
		List<StudyDto> studies = studyDao.getStudiesForUser(connection, userId);
		writeJson(response, studies);
	}
}