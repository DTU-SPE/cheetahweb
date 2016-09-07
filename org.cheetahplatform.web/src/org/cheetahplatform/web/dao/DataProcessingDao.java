package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessingStep;

public class DataProcessingDao extends AbstractCheetahDao {
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
		statement.close();
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

		cleanUp(keys, statement);

		return new DataProcessing(id, name, comment);
	}

	/**
	 * Selects the {@link DataProcessing}s for the given study.
	 *
	 * @param connection
	 *            the database connection
	 * @param studyId
	 *            the study's id
	 * @return a map of {@link DataProcessing} id to {@link DataProcessing}
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public Map<Long, DataProcessing> selectDataProcessingForStudy(Connection connection, Long studyId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"select * from data_processing left outer join data_processing_step on pk_data_processing = fk_data_processing where fk_study = ?");

		statement.setLong(1, studyId);
		ResultSet resultSet = statement.executeQuery();

		Map<Long, DataProcessing> idToDataProcessing = new HashMap<>();
		while (resultSet.next()) {
			long id = resultSet.getLong("pk_data_processing");
			DataProcessing dataProcessing = idToDataProcessing.get(id);
			if (dataProcessing == null) {
				String name = resultSet.getString("data_processing.name");
				String comment = resultSet.getString("comment");

				dataProcessing = new DataProcessing(id, name, comment);
				idToDataProcessing.put(id, dataProcessing);
			}

			long stepId = resultSet.getLong("pk_data_processing_step");
			int version = resultSet.getInt("version");
			String type = resultSet.getString("type");
			String name = resultSet.getString("data_processing_step.name");
			String configuration = resultSet.getString("configuration");

			dataProcessing.addStep(new DataProcessingStep(stepId, name, type, version, configuration));
		}

		cleanUp(resultSet, statement);

		return idToDataProcessing;
	}
}
