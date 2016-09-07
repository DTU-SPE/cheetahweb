package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.StudyDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.DeleteStudyRequest;
import org.cheetahplatform.web.dto.ErrorDto;
import org.cheetahplatform.web.dto.StudyDto;

public class DeleteStudyServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -8526807459923760679L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		StudyDao studyDao = new StudyDao();
		long userId = new UserDao().getUserId(connection, request);

		DeleteStudyRequest deleteStudyRequest = readJson(request, DeleteStudyRequest.class);

		String message = studyDao.canStudyBeDeleted(connection, deleteStudyRequest.getStudyId(), userId);
		if (message == null) {
			studyDao.deleteStudy(connection, deleteStudyRequest.getStudyId(), userId);
		} else {
			response.setStatus(400);
			writeJson(response, new ErrorDto(400, message));
			return;
		}

		List<StudyDto> studiesForUser = studyDao.getStudiesForUser(connection, userId);
		writeJson(response, studiesForUser);
	}
}
