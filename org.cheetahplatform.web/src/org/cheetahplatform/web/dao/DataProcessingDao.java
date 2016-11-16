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
	 * @param decimalSeparator
	 * @throws SQLException
	 */
	public DataProcessing insert(Connection connection, long studyId, String name, String comment, String timestampColumn,
			String leftPupilColumn, String rightPupilColumn, String decimalSeparator) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into data_processing (fk_study, name, comment, timestamp_column, left_pupil_column, right_pupil_column, decimal_separator) values (?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, studyId);
		statement.setString(2, name);
		statement.setString(3, comment);
		statement.setString(4, timestampColumn);
		statement.setString(5, leftPupilColumn);
		statement.setString(6, rightPupilColumn);
		statement.setString(7, decimalSeparator);
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);

		return new DataProcessing(id, name, comment, timestampColumn, leftPupilColumn, rightPupilColumn, decimalSeparator, null);
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
				String timestampColumn = resultSet.getString("timestamp_column");
				String leftPupilColumn = resultSet.getString("left_pupil_column");
				String rightPupilColumn = resultSet.getString("right_pupil_column");
				String decimalSeparator = resultSet.getString("decimal_separator");
				String trialComputationConfiguration = resultSet.getString("trial_computation_configuration");

				dataProcessing = new DataProcessing(id, name, comment, timestampColumn, leftPupilColumn, rightPupilColumn, decimalSeparator,
						trialComputationConfiguration);
				idToDataProcessing.put(id, dataProcessing);
			}

			long stepId = resultSet.getLong("pk_data_processing_step");
			if (!resultSet.wasNull()) {
				int version = resultSet.getInt("version");
				String type = resultSet.getString("type");
				String name = resultSet.getString("data_processing_step.name");
				String configuration = resultSet.getString("configuration");

				dataProcessing.addStep(new DataProcessingStep(stepId, name, type, version, configuration));
			}
		}

		cleanUp(resultSet, statement);

		return idToDataProcessing;
	}

	/**
	 * Updates a data processing.
	 *
	 * @param connection
	 * @param dataProcessingId
	 * @param timestampColumn
	 * @param leftPupilColumn
	 * @param rightPupilColumn
	 * @throws SQLException
	 */
	public void update(Connection connection, long dataProcessingId, String timestampColumn, String leftPupilColumn,
			String rightPupilColumn, String decimalSeparator) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"update data_processing set timestamp_column = ?, left_pupil_column = ?, right_pupil_column = ?, decimal_separator = ? where pk_data_processing = ?");
		statement.setString(1, timestampColumn);
		statement.setString(2, leftPupilColumn);
		statement.setString(3, rightPupilColumn);
		statement.setString(4, decimalSeparator);
		statement.setLong(5, dataProcessingId);
		statement.executeUpdate();
	}

	/**
	 * Updates the trial computation configuration of a data processing.
	 *
	 * @param connection
	 * @param dataProcessingId
	 * @param trialConfiguration
	 * @throws SQLException
	 */
	public void updateTrialConfiguration(Connection connection, long dataProcessingId, String trialConfiguration) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement("update data_processing set trial_computation_configuration = ? where pk_data_processing  = ?");
		statement.setString(1, trialConfiguration);
		statement.setLong(2, dataProcessingId);
		statement.executeUpdate();
	}
}
