package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;

/**
 * This class is intended to create the mapping of process instances to subjects in the database. For this, the experimental process
 * instance is inspected to identify the Matrikelnummer, which is mapped to the subject in the database. The subjects have to be in the
 * database at this point in order to complete the mapping. Additionally, the modeling process instances for familiarization and modeling
 * task are mapped.
 *
 * NOTE: This class process ONLY experimental workflows with workflow configuration id 22!
 *
 * @author Jakob
 */
public class SubjectMapper {
	private static final String INTRODUCTION_MODELING_2_1_0_AUDITTRAIL_ENTRY = "introduction_modeling_2_1.0";
	private static final String INTRODUCTION_MODELING_1_1_0_AUDITTRAIL_ENTRY = "introduction_modeling_1_1.0";
	private static final String TASK_MODELING_1_0_AUDITTRAIL_ENTRY = "task_modeling_1.0";
	private static final String FAMILIARIZATION_MODELING_1_0_AUDITTRAIL_ENTRY = "familiarization_modeling_1.0";
	private static final String SUBJECT_IDENTIFICATION_AUDITTRAIL_ENTRY = "subject_identification";
	private static final String ATTRIBUTE_WORKFLOW_CONFIGURATION_ID = "workflow_configuration_id";
	private static final String ATTRIBUTE_EXPERIMENT_ACTIVITY_ID = "experiment_activity_id";
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "cheetah";
	private static final String USER = "cep";
	private static final String SCHEMA = "2015_05_study_manuel_merged";

	private static final String PROCESS = "manuel_1.0";

	// possible values: 1, 22, 333 Set accordingly for the different sessions
	private static final String WORKFLOW_CONFIGURATION_ID = "1";

	public static boolean isEntryType(AuditTrailEntry entry, String type) {
		if (!entry.isAttributeDefined(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID)) {
			return false;
		}
		return entry.getAttribute(ATTRIBUTE_EXPERIMENT_ACTIVITY_ID).equals(type);
	}

	public static void main(String[] args) throws Exception {
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);
		new SubjectMapper(connection).run();
	}

	public static void processInstance(Connection connection, long instanceId) throws SQLException {
		System.out.println();
		System.out.println("============== Processing Instance " + instanceId + " ==============");

		ProcessInstance instance = DatabasePromReader.readProcessInstance(instanceId, connection);

		if (!instance.isAttributeDefined(ATTRIBUTE_WORKFLOW_CONFIGURATION_ID)) {
			System.out.println("Skipping instance " + instanceId + ". This is not an experimental workflow process.");
			return;
		}

		String workflowId = instance.getAttribute(ATTRIBUTE_WORKFLOW_CONFIGURATION_ID);
		if (!WORKFLOW_CONFIGURATION_ID.equals(workflowId)) {
			System.out.println("Skipping instance " + instanceId + ". This is not a experimental workflow process with configuration id "
					+ WORKFLOW_CONFIGURATION_ID);
			return;
		}

		String matrikelNummer = null;
		List<String> idsToUpdate = new ArrayList<>();
		idsToUpdate.add(instance.getId());

		List<AuditTrailEntry> entries = instance.getEntries();
		for (AuditTrailEntry entry : entries) {
			if (isEntryType(entry, SUBJECT_IDENTIFICATION_AUDITTRAIL_ENTRY)) {
				matrikelNummer = entry.getAttribute("Matrikelnummer");
			}

			if (isEntryType(entry, FAMILIARIZATION_MODELING_1_0_AUDITTRAIL_ENTRY)) {
				idsToUpdate.add(entry.getAttribute("process_instance"));
			}

			if (isEntryType(entry, TASK_MODELING_1_0_AUDITTRAIL_ENTRY)) {
				idsToUpdate.add(entry.getAttribute("process_instance"));
			}

			if (isEntryType(entry, INTRODUCTION_MODELING_1_1_0_AUDITTRAIL_ENTRY)) {
				idsToUpdate.add(entry.getAttribute("process_instance"));
			}
			if (isEntryType(entry, INTRODUCTION_MODELING_2_1_0_AUDITTRAIL_ENTRY)) {
				idsToUpdate.add(entry.getAttribute("process_instance"));
			}
		}

		System.out.println("Matrikelnummer for subject is " + matrikelNummer);
		if (idsToUpdate.isEmpty()) {
			System.err.println("Did not find any process instances to update.");
			System.err.println("Skipping instance.");
			return;
		}

		System.out.println("Should update the following ids: " + idsToUpdate);

		PreparedStatement selectSubjectIdStatement = connection.prepareStatement("select pk_subject from subject where subject_id=?");
		selectSubjectIdStatement.setString(1, matrikelNummer);

		ResultSet result = selectSubjectIdStatement.executeQuery();
		if (!result.next()) {
			System.err.println("Could not find subject with matrikelnummer " + matrikelNummer);
			System.err.println("Skipping instance.");
			return;
		}

		long subjectId = result.getLong(1);

		if (result.next()) {
			System.err.println("Found more than one subject with " + matrikelNummer);
			System.err.println("Skipping instance.");
			return;
		}

		selectSubjectIdStatement.close();

		System.out.println("Subject id is " + subjectId);

		String string = idsToUpdate.toString();
		String sql = "UPDATE process_instance SET fk_subject=" + subjectId + " WHERE id in (" + string.substring(1, string.length() - 1)
				+ ")";
		System.out.println(sql);

		PreparedStatement updateStatement = connection.prepareStatement(sql);
		int affectedLines = updateStatement.executeUpdate();
		System.out.println("Updated " + affectedLines + " lines in process instance table.");
		updateStatement.close();
	}

	private Connection connection;

	public SubjectMapper(Connection connection) throws Exception {
		this.connection = connection;
	}

	public void run() throws Exception {
		System.out.println("----------------------------");
		System.out.println("Mapping Subjects");
		PreparedStatement selectProcessInstancesStatement = connection
				.prepareStatement("select pi.database_id from process_instance pi, process p where pi.process=p.database_id and p.id='"
						+ PROCESS + "'");
		ResultSet result = selectProcessInstancesStatement.executeQuery();
		while (result.next()) {
			long processInstanceId = result.getLong(1);
			processInstance(connection, processInstanceId);
			System.err.flush();
			System.out.flush();
		}

		selectProcessInstancesStatement.close();
	}
}
