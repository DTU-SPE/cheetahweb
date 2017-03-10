package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dto.PpmInstanceDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class PpmInstanceDao extends AbstractCheetahDao {

	public List<PpmInstanceDto> selectProcessInstancesForSubjectAndTask(Connection connection, long subjectId, String experimentTask)
			throws SQLException {
		String query = "SELECT process_instance.database_id, process.id, process.database_id FROM process_instance join process on process.database_id = process_instance.process WHERE fk_subject = ? and process.id = ?;";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, subjectId);
		statement.setString(2, experimentTask);
		ResultSet resultSet = statement.executeQuery();

		List<PpmInstanceDto> ppmInstanceIds = new ArrayList<>();
		while (resultSet.next()) {
			long instanceId = resultSet.getLong("process_instance.database_id");
			String process = resultSet.getString("process.id");
			long processId = resultSet.getLong("process.database_id");
			PpmInstanceDto ppmInstance = new PpmInstanceDto(instanceId, process, processId);
			ppmInstanceIds.add(ppmInstance);
		}

		statement.close();

		return ppmInstanceIds;
	}

	public PpmInstanceDto selectPpmInstance(long ppmInstanceId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement(
				"select pi.database_id, p.id, p.database_id from process_instance pi, process p where pi.process=p.database_id and pi.database_id=?;");

		statement.setLong(1, ppmInstanceId);

		PpmInstanceDto ppmInstance = null;

		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long instanceId = result.getLong(1);
			String process = result.getString(2);
			long processId = result.getLong(3);
			ppmInstance = new PpmInstanceDto(instanceId, process, processId);
		}

		cleanUp(connection, statement, result);
		return ppmInstance;
	}
}
