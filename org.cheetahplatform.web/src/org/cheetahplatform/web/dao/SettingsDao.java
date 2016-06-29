package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettingsDao extends AbstractCheetahDao {

	public Map<String, String> getAllSettings(Connection connection) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select key_column, value from settings");
		ResultSet result = statement.executeQuery();

		Map<String, String> settings = new HashMap<>();
		while (result.next()) {
			String key = result.getString(1);
			String value = result.getString(2);
			settings.put(key, value);
		}

		result.close();
		statement.close();
		return settings;
	}
}
