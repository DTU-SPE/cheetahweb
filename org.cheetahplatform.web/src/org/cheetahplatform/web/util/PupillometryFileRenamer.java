package org.cheetahplatform.web.util;

import java.io.File;

/**
 * Simple tool to rename tsv files. All files should be in the same folder with the name pattern: {subjectId}.tsv For example 0315678.tsv is
 * renamed to 0315678@modeling_task_name.tsv
 *
 * @author Jakob
 */
public class PupillometryFileRenamer {
	/**
	 * The name of the modeling task.
	 */
	private static final String TASK_NAME = "task_modeling_1.0";
	/**
	 * Path to the root directory of the video export.
	 */
	private static final String ROOT = "G:/StudieManuel/Pupillometry/Fall15";

	public static void main(String[] args) {
		File root = new File(ROOT);
		if (!root.isDirectory()) {
			System.err.println("This is not a directory! Exit.");
			return;
		}

		File[] files = root.listFiles();
		for (File file : files) {
			String name = file.getName();
			int extensionStartIndex = name.lastIndexOf(".");
			String extension = name.substring(extensionStartIndex);
			if (!".tsv".equals(extension)) {
				System.err.println(name + " is not a tsv file! Skipped!");
				continue;
			}

			String subject = name.substring(0, extensionStartIndex);

			String newName = subject + "@" + TASK_NAME + extension;
			File newFile = new File(ROOT + "/" + newName);
			file.renameTo(newFile);
			System.out.println("Renamed '" + name + "' to '" + newName + "'.");
		}

		System.out.println("Done!");
	}
}
