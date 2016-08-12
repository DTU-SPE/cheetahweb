package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;

public class DataProcessingDao {
	/**
	 * Deletes the data processing with the given id.
	 *
	 * @param connection
	 * @param id
	 * @throws SQLException
	 */
	public void delete(Connection connection, long id) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("delete from data_processing where pk_data_processing = ?");
		statement.setLong(1, id);
		statement.execute();
	}

	/**
	 * Inserts a new data processing for a given study.
	 *
	 * @param connection
	 * @param studyId
	 * @param name
	 * @param comment
	 * @throws SQLException
	 */
	public DataProcessing insert(Connection connection, long studyId, String name, String comment) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("insert into data_processing (fk_study, name, comment) values (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, studyId);
		statement.setString(2, name);
		statement.setString(3, comment);
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);

		return new DataProcessing(id, name, comment);
	}
}
