package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.cheetahplatform.common.Activator;
import org.cheetahplatform.common.Assert;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.common.logging.DataContainer;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.common.logging.db.DatabaseUtil;

/**
 * Inserts the missing experiment_process_instance attributes (#426) / updates it, if already present (#427).
 *
 * @author stefan.zugal
 *
 */
public class ExperimentProcessInstanceUpdater {
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "mysql";
	private static final String USER = "root";
	private static final String SCHEMA = "2015_05_study_manuel_merged";

	public static void main(String[] args) throws Exception {
		Activator.loadMySQLDriver();
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);
		connection.setAutoCommit(false);
		ResultSet processInstanceIdResult = connection.createStatement().executeQuery("select database_id from process_instance");
		PreparedStatement selectDataStatement = connection.prepareStatement("select data from process_instance where id = ?");
		PreparedStatement updateDataStatement = connection.prepareStatement("update process_instance set data = ? where id = ?");

		while (processInstanceIdResult.next()) {
			long id = processInstanceIdResult.getLong(1);
			ProcessInstance experiment = DatabasePromReader.readProcessInstance(id, connection);

			// process experiments only
			if (!experiment.isAttributeDefined("workflow_configuration_id")) {
				continue;
			}

			for (AuditTrailEntry entry : experiment.getEntries()) {
				if (entry.getEventType().equals("BPMN_MODELING")) {
					String modelingProcessInstanceId = entry.getAttribute("process_instance");
					selectDataStatement.setString(1, modelingProcessInstanceId);
					ResultSet modelingResult = selectDataStatement.executeQuery();
					modelingResult.next();

					String data = modelingResult.getString(1);
					DataContainer container = new DataContainer();
					container.addAttributes(DatabaseUtil.fromDataBaseRepresentation(data));
					container.setAttribute("experiment_process_instance", experiment.getId());
					updateDataStatement.setString(1, DatabaseUtil.toDatabaseRepresentation(container.getAttributes()));
					updateDataStatement.setString(2, modelingProcessInstanceId);
					int affectedRows = updateDataStatement.executeUpdate();
					Assert.isTrue(affectedRows == 1);
					Assert.isTrue(!modelingResult.next());
				}
			}
		}

		connection.commit();
		connection.close();
	}
}
