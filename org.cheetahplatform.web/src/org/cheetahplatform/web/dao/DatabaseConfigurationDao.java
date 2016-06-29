package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dto.DatabaseConfigurationDto;

public class DatabaseConfigurationDao {
	public void deleteConfiguration(Connection connection, long toDelete) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement("delete from database_configuration where pk_database_configuration = ?;");
		statement.setLong(1, toDelete);
		statement.execute();

		statement.close();
	}

	public List<DatabaseConfigurationDto> getDatabaseConfigurations(Connection connection, long userId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select * from database_configuration where fk_user = ?");
		statement.setLong(1, userId);
		ResultSet result = statement.executeQuery();

		List<DatabaseConfigurationDto> configurations = new ArrayList<>();
		while (result.next()) {
			long id = result.getLong("pk_database_configuration");
			String host = result.getString("host");
			int port = result.getInt("port");
			String schema = result.getString("schema");
			String user = result.getString("user");
			String password = result.getString("password");
			DatabaseConfigurationDto current = new DatabaseConfigurationDto(id, host, port, schema, password, user);
			configurations.add(current);
		}
		result.close();
		statement.close();

		return configurations;
	}

	public DatabaseConfigurationDto insertDatabaseConfiguration(Connection connection, long user, String host, int port, String schema,
			String username, String password) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into database_configuration (fk_user,host,port,`schema`,user,password) values (?,?,?,?,?,?);",
				PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setLong(1, user);
		statement.setString(2, host);
		statement.setInt(3, port);
		statement.setString(4, schema);
		statement.setString(5, username);
		statement.setString(6, password);
		statement.execute();

		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		long id = generatedKeys.getLong(1);
		statement.close();

		return new DatabaseConfigurationDto(id, host, port, schema, password, username);
	}
}
