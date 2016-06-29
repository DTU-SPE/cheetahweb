package org.cheetahplatform.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.cheetahplatform.common.Activator;

/**
 * A simple class to create subjects in the database. Use a csv file as input. The first line (=header) is ignored. The remaining lines
 * should be in the following format: column 1 = subject; id column 2 = email; column 3 = comment
 *
 * !! Note that the study has to be in the database to run the script. !!
 *
 * @author Jakob
 */
public class SubjectImporter {
	private static final String SUBJECT_FILE_PATH = "D://subjects.csv";
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "cheetah";
	private static final String USER = "cep";
	private static final String SCHEMA = "cheetah2_0";

	public static void main(String[] args) throws Exception {
		Activator.loadMySQLDriver();
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);

		SubjectImporter importer = new SubjectImporter(connection, 2);
		importer.run();
	}

	// Make sure the study is already in the database!
	private int studyId = 2;

	private Connection connection;

	public SubjectImporter(Connection connection, int studyId) {
		this.connection = connection;
		this.studyId = studyId;
	}

	public void run() throws Exception {
		System.out.println("-------------------");
		System.out.println("Inserting subjects");
		PreparedStatement insertStatement = connection
				.prepareStatement("insert into subject (subject_id,email,comment,fk_study)VALUES (?, ?, ?, ?)");

		BufferedReader reader = new BufferedReader(new FileReader(new File(SUBJECT_FILE_PATH)));
		String line = reader.readLine();
		// go to first content
		line = reader.readLine();

		while (line != null) {
			String[] splitted = line.split(";");
			if (splitted.length < 3) {
				continue;
			}

			String subjectId = splitted[0];
			long parsedId = Long.parseLong(subjectId);
			// make sure the leading 0 is added for matrikel numbers before 2010.
			if (parsedId < 1000000) {
				subjectId = "0" + String.valueOf(parsedId);
			}

			insertStatement.setString(1, subjectId);
			insertStatement.setString(2, splitted[1]);
			insertStatement.setString(3, splitted[2]);
			insertStatement.setLong(4, studyId);
			insertStatement.executeUpdate();

			line = reader.readLine();
			System.out.println("Inserted subject with id: " + subjectId);
		}

		reader.close();
		System.out.println("Import complete!");
	}
}
