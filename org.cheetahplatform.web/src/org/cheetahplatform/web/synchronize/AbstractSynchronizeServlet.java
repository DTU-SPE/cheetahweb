package org.cheetahplatform.web.synchronize;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.DatabaseConfigurationDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

import com.fasterxml.jackson.core.JsonParseException;

public abstract class AbstractSynchronizeServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -4367565269665848614L;

	/**
	 * Carries out a computation with source and target connection.
	 *
	 * @param sourceConnection
	 * @param targetConnection
	 * @param req
	 * @param resp
	 */
	protected abstract void doPostWith(Connection sourceConnection, Connection targetConnection, HttpServletRequest req,
			HttpServletResponse resp) throws Exception;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException {
		Connection sourceConnection = null;

		try {
			DatabaseConfigurationDto configuration = getConfiguration(req);
			sourceConnection = DriverManager.getConnection(configuration.asMysqlUrl(), configuration.getUsername(),
					configuration.getPassword());

			doPostWith(sourceConnection, connection, req, resp);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (sourceConnection != null) {
				try {
					sourceConnection.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}

	protected DatabaseConfigurationDto getConfiguration(HttpServletRequest req) throws JsonParseException, IOException {
		return readJson(req, DatabaseConfigurationDto.class);
	}
}
