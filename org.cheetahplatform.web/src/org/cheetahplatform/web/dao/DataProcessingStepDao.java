package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataProcessingStepDao {

	/**
	 * Inserts a new data processing step.
	 *
	 * @param connection
	 * @param dataProcessingId
	 * @param type
	 * @param name
	 * @param configuration
	 * @return the generated key for the step
	 * @throws SQLException
	 */
	public long insert(Connection connection, long dataProcessingId, String type, String name, String configuration) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into data_processing_step (fk_data_processing, type, version, name, configuration) values (?,?,?,?,?)",
				Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, dataProcessingId);
		statement.setString(2, type);
		statement.setInt(3, 1);
		statement.setString(4, name);
		statement.setString(5, configuration);
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		return keys.getLong(1);
	}

}
