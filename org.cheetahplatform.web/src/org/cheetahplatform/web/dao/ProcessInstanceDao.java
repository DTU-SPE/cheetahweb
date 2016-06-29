package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.common.logging.db.DatabaseIdGenerator;
import org.cheetahplatform.web.dto.PlainProcessDto;
import org.cheetahplatform.web.dto.PlainProcessInstanceDto;
import org.cheetahplatform.web.dto.PlainSubjectDto;

public class ProcessInstanceDao {
	private List<PlainProcessInstanceDto> extractProcessInstances(PreparedStatement statement) throws SQLException {
		ResultSet resultSet = statement.executeQuery();

		List<PlainProcessInstanceDto> processInstances = new ArrayList<>();
		while (resultSet.next()) {
			long databaseId = resultSet.getLong("database_id");
			long process = resultSet.getLong("process");
			String id = resultSet.getString("id");
			String data = resultSet.getString("data");
			long subject = resultSet.getLong("fk_subject");
			long synchronizedFrom = resultSet.getLong("synchronized_from");

			processInstances.add(new PlainProcessInstanceDto(databaseId, process, id, data, subject, synchronizedFrom));
		}
		resultSet.close();
		statement.close();
		return processInstances;
	}

	public List<PlainProcessInstanceDto> getProcessInstancesForSubject(Connection connection, long subjecId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select * from process_instance where fk_subject = ?");
		statement.setLong(1, subjecId);

		return extractProcessInstances(statement);
	}

	public PlainProcessInstanceDto insertSynchronized(Connection connection, PlainProcessInstanceDto source, PlainProcessDto targetProcess,
			PlainSubjectDto targetSubject) throws SQLException {
		String newId = new DatabaseIdGenerator().generateId(connection);

		PreparedStatement statement = connection.prepareStatement(
				"insert into process_instance (process, id, data, fk_subject, synchronized_from) values (?, ?, ?, ?, ?)",
				PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setLong(1, targetProcess.getDatabaseId());
		statement.setString(2, newId);
		statement.setString(3, source.getData());
		statement.setLong(4, targetSubject.getId());
		statement.setLong(5, source.getDatabaseId());
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long databaseId = keys.getLong(1);
		statement.close();

		return new PlainProcessInstanceDto(databaseId, targetProcess.getDatabaseId(), newId, source.getData(), targetSubject.getId(),
				source.getDatabaseId());
	}

	public void updateDataAttribute(Connection connection, PlainProcessInstanceDto processInstance, String data) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("update process_instance set data = ? where database_id = ?;");
		statement.setString(1, data);
		statement.setLong(2, processInstance.getDatabaseId());
		int affectedRows = statement.executeUpdate();
		if (affectedRows != 1) {
			throw new RuntimeException("Unexpected row count: " + affectedRows);
		}

		statement.close();
	}

}
