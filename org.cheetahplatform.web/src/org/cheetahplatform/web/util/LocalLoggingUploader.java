package org.cheetahplatform.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.cheetahplatform.common.Activator;
import org.eclipse.core.runtime.Assert;

/**
 * A little class to help upload files of a local logger. The class assumes a folder containing folders for each subject. The mxml files in
 * the subfolders are zipped and uploaded to the target database.
 *
 * @author Jakob
 */
public class LocalLoggingUploader {
	private static final int PORT = 3306;
	private static final String HOST = "localhost";
	private static final String PASSWORD = "";
	private static final String USER = "cep";
	private static final String SCHEMA = "study_manuel_final";

	private static final String ROOT = "C:/Users/Jakob/Desktop/Manuel_processed/xml_logs_from_workspace_folder";

	public static void main(String[] args) throws Exception {
		File root = new File(ROOT);
		if (!root.isDirectory()) {
			System.err.println(ROOT + "is not a directory.");
			return;
		}

		Activator.loadMySQLDriver();
		Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + String.valueOf(PORT) + "/" + SCHEMA, USER,
				PASSWORD);

		File[] subfolders = root.listFiles();
		for (File subfolder : subfolders) {
			if (!subfolder.isDirectory()) {
				System.err.println(subfolder.getAbsolutePath() + " is not a directory. Skipped.");
				continue;
			}

			System.out.println("Processing " + subfolder.getName());

			List<File> mxmlFiles = new ArrayList<>();

			for (File mxmlFile : subfolder.listFiles()) {
				if (!mxmlFile.getName().endsWith("mxml")) {
					System.err.println(mxmlFile.getAbsolutePath() + " is not a log file. Skipped.");
					continue;
				}
				mxmlFiles.add(mxmlFile);
			}

			File zipFile = new File(subfolder.getAbsolutePath() + "/xmlLog.zip");
			System.out.println("Writing zip file " + zipFile.getAbsolutePath() + "...");
			FileOutputStream buffer = new FileOutputStream(zipFile);
			ZipOutputStream out = new ZipOutputStream(buffer);
			byte[] readBuffer = new byte[10240];
			for (File logFile : mxmlFiles) {
				out.putNextEntry(new ZipEntry(logFile.getName()));
				FileInputStream in = new FileInputStream(logFile);

				int read = in.read(readBuffer, 0, readBuffer.length);
				while (read != -1) {
					out.write(readBuffer, 0, read);
					read = in.read(readBuffer, 0, readBuffer.length);
				}

				in.close();
				out.closeEntry();
			}

			out.close();
			System.out.println("Zipfile complete. Starting upload...");

			FileInputStream input = new FileInputStream(zipFile);
			byte[] bytes = IOUtils.toByteArray(input);
			PreparedStatement statement = connection.prepareStatement("insert into xml_log (log) values (?);"); //$NON-NLS-1$
			statement.setBytes(1, bytes);
			int result = statement.executeUpdate();
			Assert.isTrue(result == 1);
			statement.close();
			input.close();
			System.out.println("Upload complete.");
		}

		connection.close();
		System.out.println("Done!");
	}
}
