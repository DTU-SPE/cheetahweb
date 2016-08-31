package org.cheetahplatform.web.eyetracking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.dto.ConnectRequest;

public abstract class AbstractConnectWorkItem extends AbstractCheetahWorkItem {
	protected ConnectRequest request;

	public AbstractConnectWorkItem(long userId, long fileId, ConnectRequest request, String message) {
		super(userId, message);

		this.fileId = fileId;
		this.request = request;
	}

	protected Long getProcessInstanceId(Connection connection, String subjectName, String experimentTask) throws SQLException {
		String query = "SELECT process_instance.database_id FROM process_instance join process on process.database_id = process_instance.process   WHERE fk_subject IN (SELECT pk_subject FROM subject WHERE subject_id = ?) and process.id = ?;";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, subjectName);
		statement.setString(2, experimentTask);
		ResultSet resultSet = statement.executeQuery();
		Long id = null;
		if (resultSet.next()) {
			id = resultSet.getLong("process_instance.database_id");
		}
		statement.close();

		return id;
	}
}
