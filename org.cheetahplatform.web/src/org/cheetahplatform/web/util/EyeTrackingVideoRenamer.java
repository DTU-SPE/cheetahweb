package org.cheetahplatform.web.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.runtime.Assert;

/**
 * Intended to rename the output created by the exporter of the eye tracker. The script assumes that each video is in it's own file. All
 * files are renamed according to the structure suitable for CEP web and placed in a newly created out directory.
 *
 * @author Jakob
 */
public class EyeTrackingVideoRenamer {
	/**
	 * The name of the modeling task.
	 */
	private static final String TASK_NAME = "task_modeling_1.0";
	/**
	 * Path to the root directory of the video export.
	 */
	private static final String ROOT = "F:/StudieManuel/PupillometryComplete_Splitted_Fall15_rename";

	public static void main(String[] args) {
		File root = new File(ROOT);
		Assert.isTrue(root.isDirectory());

		String outPath = ROOT + "/out";
		boolean outCreated = new File(outPath).mkdir();
		if (!outCreated) {
			System.err.println("Failed to created output directory. Stopped.");
			return;
		}

		File[] children = root.listFiles();
		for (File file : children) {
			if (file.getName().equals("out")) {
				continue;
			}
			System.out.println("Processing " + file.getName());

			if (!file.isDirectory()) {
				System.err.println(file.getName() + " is not a directory - skipped.");
				continue;
			}

			File[] leafs = file.listFiles();
			if (leafs.length != 1) {
				System.err.println("Did not find the expected number of files in " + file.getName() + ". Skipped!");
				continue;
			}

			File movie = leafs[0];
			String name = movie.getName();
			String id = name.substring(name.indexOf("_") + 1, name.lastIndexOf("."));

			File renamed = new File(movie.getParentFile().getAbsolutePath() + "/" + id + "@" + TASK_NAME + ".avi");

			boolean renameSuccess = movie.renameTo(renamed);
			if (!renameSuccess) {
				System.err.println("Failed to rename " + movie.getName() + ". Skipped!");
				continue;
			}

			try {
				Files.move(renamed.toPath(), new File(outPath + "/" + renamed.getName()).toPath());
			} catch (IOException e) {
				System.err.println("Failed to copy file " + renamed.getName());
				e.printStackTrace();
			}
		}

		System.out.println("Complete!");
	}
}
