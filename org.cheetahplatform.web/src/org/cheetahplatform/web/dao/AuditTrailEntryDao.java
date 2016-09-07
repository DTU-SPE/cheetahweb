package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cheetahplatform.web.dto.PlainAuditTrailEntryDto;

public class AuditTrailEntryDao {
	public List<PlainAuditTrailEntryDto> getAuditTrailEntriesForProcessInstance(Connection connection, long processInstance)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select * from audittrail_entry where process_instance = ?");
		statement.setLong(1, processInstance);
		ResultSet resultSet = statement.executeQuery();
		List<PlainAuditTrailEntryDto> auditTrailEntries = new ArrayList<>();
		while (resultSet.next()) {
			long id = resultSet.getLong("database_id");
			Date timestamp = new Date(Long.parseLong(resultSet.getString("timestamp")));
			String type = resultSet.getString("type");
			String workflowElement = resultSet.getString("workflow_element");
			String originator = resultSet.getString("originator");
			String data = resultSet.getString("data");

			auditTrailEntries.add(new PlainAuditTrailEntryDto(id, processInstance, timestamp, type, workflowElement, originator, data));

		}
		resultSet.close();
		statement.close();
		return auditTrailEntries;
	}

	public void insertSynchronized(Connection connection, List<PlainAuditTrailEntryDto> sourceAuditTrailEntries,
			long processInstanceDatabaseId, String processInstanceId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into audittrail_entry (process_instance, timestamp, type, workflow_element, originator, data, synchronized_from) values (?, ?, ?, ?, ?, ?, ?)");
		for (PlainAuditTrailEntryDto entry : sourceAuditTrailEntries) {
			statement.setLong(1, processInstanceDatabaseId);
			statement.setString(2, String.valueOf(entry.getTimestamp().getTime()));
			statement.setString(3, entry.getType());
			statement.setString(4, entry.getWorkflowElement());
			statement.setString(5, entry.getOriginator());
			statement.setString(6, entry.getData());
			statement.setLong(7, entry.getId());

			statement.addBatch();
		}

		statement.executeBatch();
		statement.close();
	}

	public void updateDataAttribute(Connection connection, PlainAuditTrailEntryDto entry, String data) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("update audittrail_entry set data = ? where database_id = ?;");
		statement.setString(1, data);
		statement.setLong(2, entry.getId());
		int affectedRows = statement.executeUpdate();
		if (affectedRows != 1) {
			throw new RuntimeException("Unexpected row count: " + affectedRows);
		}

		statement.close();
	}
}
