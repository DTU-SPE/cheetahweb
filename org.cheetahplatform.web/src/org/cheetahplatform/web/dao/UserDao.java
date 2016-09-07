package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import org.cheetahplatform.web.dto.CreateUserRequest;
import org.cheetahplatform.web.dto.StudyDto;
import org.cheetahplatform.web.dto.UserCredentialsDto;
import org.cheetahplatform.web.dto.UserDto;

public class UserDao {
	public long getUserId(Connection connection, HttpServletRequest request) throws SQLException {
		String email = request.getUserPrincipal().getName();
		return getUserId(connection, email);
	}

	public long getUserId(java.sql.Connection connection, String email) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select pk_user from user_table where email=?");
		statement.setString(1, email);
		ResultSet result = statement.executeQuery();
		Long userId = null;
		while (result.next()) {
			userId = result.getLong(1);
		}

		result.close();
		return userId;
	}

	public UserDto getUserInformation(Connection connection, long userId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"select firstname, lastname, user_table.email, role from user_table join user_roles on fk_user = pk_user where pk_user = ?;");
		statement.setLong(1, userId);
		ResultSet result = statement.executeQuery();
		UserDto user = null;
		while (result.next()) {
			String firstname = result.getString(1);
			String lastname = result.getString(2);
			String email = result.getString(3);
			String role = result.getString(4);
			user = new UserDto(firstname, lastname, email, role);
		}

		statement.close();
		result.close();
		return user;
	}

	public void insertUser(Connection connection, CreateUserRequest request) throws SQLException {
		// insert the user
		PreparedStatement insertUserStatement = connection.prepareStatement(
				"insert into user_table (firstname, lastname, email, password) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		insertUserStatement.setString(1, request.getFirstname());
		insertUserStatement.setString(2, request.getLastname());
		insertUserStatement.setString(3, request.getEmail());
		insertUserStatement.setString(4, request.getHash());
		insertUserStatement.execute();
		ResultSet keys = insertUserStatement.getGeneratedKeys();
		keys.next();
		long userId = keys.getLong(1);
		insertUserStatement.close();

		// add the user role, defaults to "user"
		PreparedStatement insertRoleStatement = connection
				.prepareStatement("insert into user_roles (role, email, fk_user) values (?, ?, ?)");
		insertRoleStatement.setString(1, UserDto.ROLE_USER);
		insertRoleStatement.setString(2, request.getEmail());
		insertRoleStatement.setLong(3, userId);
		insertRoleStatement.execute();

		// add a default study for the user
		StudyDao studyDao = new StudyDao();
		StudyDto study = new StudyDto(null, "Default study for user " + request.getEmail(),
				"This is the default study generated for user " + request.getEmail());
		studyDao.insertSynchronizedStudy(connection, study, userId);
	}

	public boolean isOldPasswordCorrect(Connection connection, UserCredentialsDto credentials) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select password from user_table where email=?");
		statement.setString(1, credentials.getEmail());
		ResultSet result = statement.executeQuery();
		String password = null;
		while (result.next()) {
			password = result.getString(1);
		}
		result.close();
		statement.close();

		if (password == null) {
			return false;
		}
		return password.equals(credentials.getOldHash());
	}

	public void setPassword(Connection connection, UserCredentialsDto credentials) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("update user_table set password=? where email=?");
		statement.setString(1, credentials.getNewHash());
		statement.setString(2, credentials.getEmail());

		statement.executeUpdate();
		statement.close();
	}

	public boolean userExists(Connection connection, String email) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select count(*) from user_table where email = ?");
		statement.setString(1, email);
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		int existingUserCount = resultSet.getInt(1);
		return existingUserCount > 0;
	}
}
