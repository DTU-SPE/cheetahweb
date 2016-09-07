package org.cheetahplatform.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.io.IOUtils;
import org.cheetahplatform.common.Activator;
import org.eclipse.core.runtime.Assert;

/**
 * A class intended to download the log zip files of one database, store all zip files locally and upload the files to a different database.
 *
 * @author Jakob
 *
 */
public class XmlLogDownloader {
	private static final String TARGET_DIRECTORY = "C:/Users/Jakob/Desktop/download";
	private static final int SOURCE_PORT = 13306;
	private static final String SOURCE_HOST = "138.232.65.123";
	private static final String SOURCE_PASSWORD = "";
	private static final String SOURCE_USER = "coffee_admin";
	private static final String SOURCE_SCHEMA = "2015_05_study_manuel";

	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "";
	private static final String USER = "cep";
	private static final String SCHEMA = "study_manuel_final";

	public static void main(String[] args) throws Exception {
		Activator.loadMySQLDriver();

		Connection connection = DriverManager.getConnection("jdbc:mysql://" + SOURCE_HOST + ":" + String.valueOf(SOURCE_PORT) + "/"
				+ SOURCE_SCHEMA, SOURCE_USER, SOURCE_PASSWORD);
		PreparedStatement statement = connection
				.prepareStatement("select database_id, timestamp, log from xml_log where database_id > 697");
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long id = result.getLong(1);
			System.out.println("Copying id: " + id);
			Timestamp timestamp = result.getTimestamp(2);
			Blob blob = result.getBlob(3);
			InputStream binaryStream = blob.getBinaryStream();
			FileOutputStream fileOutputStream = new FileOutputStream(new File(TARGET_DIRECTORY + "/" + id + "@" + timestamp.getTime()
					+ ".zip"));

			System.out.println("Writing file to disk...");
			IOUtils.copy(binaryStream, fileOutputStream);

			writeToTargetDatabase(id, timestamp, blob);

			System.out.println("File with id " + id + " complete!");
		}
		result.close();
		connection.close();

		System.out.println("Done!");
	}

	private static void writeToTargetDatabase(long id, Timestamp timestamp, Blob blob) throws SQLException {
		System.out.println("Uploading file to target database...");
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO xml_log (database_id, timestamp, log) VALUES (?,?,?)");
		insertStatement.setLong(1, id);
		insertStatement.setTimestamp(2, timestamp);
		insertStatement.setBlob(3, blob);
		int result = insertStatement.executeUpdate();
		Assert.isTrue(result == 1, "Illegal number of rows touched!");
		insertStatement.close();
		connection.close();
	}
}
