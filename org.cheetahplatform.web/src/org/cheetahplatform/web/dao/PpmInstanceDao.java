package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cheetahplatform.web.dto.PpmInstanceDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class PpmInstanceDao extends AbstractCheetahDao {

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
