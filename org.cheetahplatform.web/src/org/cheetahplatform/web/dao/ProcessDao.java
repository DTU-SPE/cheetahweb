package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dto.PlainProcessDto;

public class ProcessDao {
	public PlainProcessDto insertSynchronized(Connection connection, PlainProcessDto toInsert) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into process (id,data,type,notation,synchronized_from) values (?, ?, ?, ?, ?)",
				PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setString(1, toInsert.getId());
		statement.setString(2, toInsert.getData());
		statement.setString(3, toInsert.getType());
		statement.setString(4, toInsert.getNotation());
		statement.setLong(5, toInsert.getDatabaseId());
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);
		statement.close();

		return new PlainProcessDto(id, toInsert.getId(), toInsert.getData(), toInsert.getType(), toInsert.getNotation(),
				toInsert.getDatabaseId());
	}

	public List<PlainProcessDto> selectAll(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from process");

		List<PlainProcessDto> all = new ArrayList<>();
		while (resultSet.next()) {
			long databaseId = resultSet.getLong("database_id");
			String id = resultSet.getString("id");
			String data = resultSet.getString("data");
			String type = resultSet.getString("type");
			String notation = resultSet.getString("notation");
			long synchronizedFrom = resultSet.getLong("synchronized_from");

			all.add(new PlainProcessDto(databaseId, id, data, type, notation, synchronizedFrom));
		}
		resultSet.close();
		statement.close();
		return all;
	}
}
