package org.cheetahplatform.web.migration;

import java.sql.Connection;
import java.sql.DriverManager;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseMigrator {
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "cheetah_web_pass";
	private static final String USER = "cheetah_web";
	private static final String SCHEMA = "cheetah_web";
	
	public static void main(String[] args) throws Exception {
		Connection connection = DriverManager
				.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER, PASSWORD);

		Liquibase liquibase = null;
		Database database = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new JdbcConnection(connection));
		liquibase = new Liquibase("resources/changeLog.xml", new FileSystemResourceAccessor(), database);
		liquibase.update("");
	}
}
