package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.CreateUserRequest;
import org.cheetahplatform.web.dto.ResponseDto;
import org.cheetahplatform.web.dto.UserDto;

public class CreateUserServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 2057615580497961482L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		CreateUserRequest createUserRequest = readJson(request, CreateUserRequest.class);
		UserDao userDao = new UserDao();
		long userId = userDao.getUserId(connection, request);
		UserDto userInformation = userDao.getUserInformation(connection, userId);

		String error = null;
		if (!userInformation.getRole().equals(UserDto.ROLE_ADMINISTRATOR)) {
			error = "Only administrators are allowed to create users.";
		} else if (userDao.userExists(connection, createUserRequest.getEmail())) {
			error = "A user with the email \"" + createUserRequest.getEmail() + "\" already exists.";
		} else {
			userDao.insertUser(connection, createUserRequest);
		}

		writeJson(response, new ResponseDto(error));
	}

}
