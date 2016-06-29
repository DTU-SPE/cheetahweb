package org.cheetahplatform.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import org.cheetahplatform.common.Activator;
import org.cheetahplatform.common.logging.Attribute;
import org.cheetahplatform.common.logging.db.DatabaseUtil;
import org.eclipse.core.runtime.Assert;

/**
 * Little program to replace attributes of audittrail entries. Adapt calls to replaceAttribute in main as needed.
 *
 * @author Jakob
 */
public class AudittrailEntryAttributeSetter {
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "";
	private static final String USER = "cep";
	private static final String SCHEMA = "study_manuel_final";
	private static Scanner SCANNER = new Scanner(System.in);

	public static void main(String[] args) throws Exception {
		Activator.loadMySQLDriver();

		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);

		AudittrailEntryAttributeSetter setter = new AudittrailEntryAttributeSetter(connection);

		setter.replaceAttribute(3637562, "Matrikelnummer", "1389560");
		setter.replaceAttribute(3802037, "Matrikelnummer", "1389560");
		setter.replaceAttribute(3776504, "Matrikelnummer", "1389560");

		setter.replaceAttribute(3929953, "Matrikelnummer", "1418966");
		setter.replaceAttribute(4106344, "Matrikelnummer", "1418966");

		connection.close();
		SCANNER.close();
		System.out.println("Done :)");
	}

	private Connection connection;

	public AudittrailEntryAttributeSetter(Connection connection) {
		this.connection = connection;
	}

	public void replaceAttribute(long id, String attributeName, String newValue) throws SQLException {
		replaceAttribute(id, attributeName, newValue, false);
	}

	public void replaceAttribute(long id, String attributeName, String newValue, boolean quiet) throws SQLException {
		System.out.println("Replacing attribute " + attributeName + " in audittrail entry with id " + id + " with value " + newValue);
		PreparedStatement statement = connection.prepareStatement("select data from audittrail_entry where database_id = ?");
		statement.setLong(1, id);
		ResultSet result = statement.executeQuery();
		String dataString = null;
		while (result.next()) {
			if (dataString != null) {
				System.err.println("More than one audit traily entry returned! Skipping attribute.");
				result.close();
				statement.close();
				return;
			}
			dataString = result.getString(1);
		}
		System.out.println(dataString);

		int toReplaceIndex = -1;
		List<Attribute> data = DatabaseUtil.fromDataBaseRepresentation(dataString);
		for (int i = 0; i < data.size(); i++) {
			Attribute attribute = data.get(i);
			if (attribute.getName().equals(attributeName)) {
				toReplaceIndex = i;
				break;
			}
		}

		if (toReplaceIndex < 0) {
			System.err.println("Could not find attribute with name " + attributeName + ". Skipped.");
			return;
		}

		data.remove(toReplaceIndex);
		Attribute newAttribute = new Attribute(attributeName, newValue);
		data.add(toReplaceIndex, newAttribute);

		String databaseRepresentation = DatabaseUtil.toDatabaseRepresentation(data);
		System.out.println(databaseRepresentation);
		String answer = null;
		if (!quiet) {
			System.out.println("Is this correct? Enter yes to replace.");
			answer = SCANNER.next();
		}

		if (quiet || "yes".equals(answer)) {
			System.out.println("Replacing attribute...");
			PreparedStatement updateStatement = connection.prepareStatement("UPDATE audittrail_entry SET data=? WHERE database_id=?");
			updateStatement.setString(1, databaseRepresentation);
			updateStatement.setLong(2, id);
			int updatedLines = updateStatement.executeUpdate();
			Assert.isTrue(updatedLines == 1);
			System.out.println("Update complete");
		} else {
			System.out.println("Nothing to do.");
		}

	}
}
