package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cheetahplatform.common.Activator;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.common.logging.DatabaseAuditTrailEntry;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;

public class RavenSolutionRepairer {
	private static final String UNDERSTANDABILITY_AUDITTRAIL_ENTRY_TYPE = "UNDERSTANDABILITY";
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "cheetah";
	private static final String USER = "cep";
	private static final String SCHEMA = "2015_05_study_manuel_merged";
	private static final String ATTRIBUTE_EXPERIMENT_ACTIVITY_ID = "experiment_activity_id";
	private static final String PROCESS = "manuel_1.0";
	private static final String WORKFLOW_CONFIGURATION_ID = "1";
	private static final String ATTRIBUTE_WORKFLOW_CONFIGURATION_ID = "workflow_configuration_id";

	public static void main(String[] args) throws SQLException {
		Activator.loadMySQLDriver();
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);
		RavenSolutionRepairer repairer = new RavenSolutionRepairer(connection);
		repairer.run();
		connection.close();
	}

	private Connection connection;

	public RavenSolutionRepairer(Connection connection) {
		this.connection = connection;
	}

	private void processInstance(long processInstanceId) throws SQLException {
		System.out.println("Processing instance: " + processInstanceId);
		ProcessInstance instance = DatabasePromReader.readProcessInstance(processInstanceId, connection);

		if (!instance.isAttributeDefined(ATTRIBUTE_WORKFLOW_CONFIGURATION_ID)) {
			System.out.println("Skipping instance " + processInstanceId + ". This is not an experimental workflow process.");
			return;
		}

		String workflowId = instance.getAttribute(ATTRIBUTE_WORKFLOW_CONFIGURATION_ID);
		if (!WORKFLOW_CONFIGURATION_ID.equals(workflowId)) {
			System.out.println("Skipping instance " + processInstanceId
					+ ". This is not a experimental workflow process with configuration id " + WORKFLOW_CONFIGURATION_ID);
			return;
		}
		AudittrailEntryAttributeSetter attributeSetter = new AudittrailEntryAttributeSetter(connection);

		List<AuditTrailEntry> entries = instance.getEntries();
		for (AuditTrailEntry entry : entries) {
			if (!entry.getEventType().equals(UNDERSTANDABILITY_AUDITTRAIL_ENTRY_TYPE)) {
				continue;
			}

			String id = entry.getAttribute(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID);
			String[] splitted = id.split("_");

			// test if already replaced
			if ("EXAMPLE".equals(splitted[1])) {
				System.out.println("Already repaired - skipping audittrail entry.");
				continue;
			}

			int questionNumber = Integer.parseInt(splitted[1]);

			// not the first set of questions!
			if (!entry.isAttributeDefined("Beispiel " + questionNumber)) {
				continue;
			}
			attributeSetter.replaceAttribute(((DatabaseAuditTrailEntry) entry).getId(), ATTRIBUTE_EXPERIMENT_ACTIVITY_ID, "RAVEN_EXAMPLE_"
					+ questionNumber, true);
		}

		System.out.println("Raven repair for instance " + processInstanceId + " complete!");
	}

	public void run() throws SQLException {
		System.out.println("----------------------------");
		System.out.println("Repairing Raven");
		PreparedStatement selectProcessInstancesStatement = connection
				.prepareStatement("select pi.database_id from process_instance pi, process p where pi.process=p.database_id and p.id='"
						+ PROCESS + "'");
		ResultSet result = selectProcessInstancesStatement.executeQuery();
		while (result.next()) {
			long processInstanceId = result.getLong(1);
			processInstance(processInstanceId);
			System.err.flush();
			System.out.flush();
		}
		selectProcessInstancesStatement.close();
	}
}
