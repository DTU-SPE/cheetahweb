package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UpdateUserDataTagsRequest;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.dto.UserFileTagDto;

import com.fasterxml.jackson.core.JsonParseException;

public class AddUserDataTagsServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 541707666188740156L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {

		try {
			UpdateUserDataTagsRequest request = readJson(req, UpdateUserDataTagsRequest.class);
			UserFileDao userFileDao = new UserFileDao();
			for (Long fileId : request.getFileIds()) {
				Set<String> tagsToAdd = new HashSet<>();
				tagsToAdd.addAll(request.getTags());
				UserFileDto file = userFileDao.getFile(fileId);
				List<UserFileTagDto> tags = file.getTags();
				for (UserFileTagDto tag : tags) {
					tagsToAdd.add(tag.getTag());
				}

				userFileDao.updateTags(fileId, tagsToAdd);
			}
		} catch (JsonParseException e) {
			// do not bother
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
