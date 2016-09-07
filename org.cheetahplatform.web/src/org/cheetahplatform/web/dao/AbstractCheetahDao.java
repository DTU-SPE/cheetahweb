package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

public class AbstractCheetahDao {

	public AbstractCheetahDao() {
		super();
	}

	protected String buildIn(Collection<? extends Object> files) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Object id : files) {
			if (!first) {
				builder.append(", ");
			}
			builder.append("'");
			builder.append(id.toString());
			first = false;
			builder.append("'");
		}
		return builder.toString();
	}

	public void cleanUp(Connection connection, Statement statement) throws SQLException {
		statement.close();
		connection.close();
	}

	public void cleanUp(Connection connection, Statement statement, ResultSet resultSet) throws SQLException {
		resultSet.close();
		cleanUp(connection, statement);
	}

	protected void cleanUp(ResultSet result, Statement statement) throws SQLException {
		result.close();
		statement.close();
	}
}