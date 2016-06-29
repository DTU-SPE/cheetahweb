package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.cheetahplatform.common.Activator;

public class StudyManuelUpdater {
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "cheetah";
	private static final String USER = "cep";
	private static final String SCHEMA = "2015_05_study_manuel_merged";

	public static void main(String[] args) throws Exception {
		Activator.loadMySQLDriver();
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);

		PreparedStatement statement = connection
				.prepareStatement("INSERT INTO study VALUES (2,'Study Manuel', 'Manuels study executed in Summer and Fall 2015');");
		statement.executeUpdate();
		statement.close();

		AudittrailEntryAttributeSetter setter = new AudittrailEntryAttributeSetter(connection);
		setter.replaceAttribute(3637562, "Matrikelnummer", "1389560");
		setter.replaceAttribute(3802037, "Matrikelnummer", "1389560");
		setter.replaceAttribute(3776504, "Matrikelnummer", "1389560");

		setter.replaceAttribute(3929953, "Matrikelnummer", "1418966");
		setter.replaceAttribute(4106344, "Matrikelnummer", "1418966");

		setter.replaceAttribute(4003038, "Matrikelnummer", "1317951");
		setter.replaceAttribute(3916706, "Matrikelnummer", "1318980");
		setter.replaceAttribute(3985465, "Matrikelnummer", "1415533");

		SubjectImporter subjectImporter = new SubjectImporter(connection, 2);
		subjectImporter.run();

		SubjectMapper subjectMapper = new SubjectMapper(connection);
		subjectMapper.run();

		connection.close();
	}
}
