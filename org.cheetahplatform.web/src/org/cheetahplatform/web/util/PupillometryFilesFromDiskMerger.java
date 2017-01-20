package org.cheetahplatform.web.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileMerger;

public class PupillometryFilesFromDiskMerger {
	private static final String TARGET = "C:\\Users\\Jakob\\Desktop\\Pupillometrie_Studie_Thomas_18012017\\Studie_Thomas_Merged.tsv";
	public static List<File> FOLDERS;

	static {
		FOLDERS = new ArrayList<>();
		FOLDERS.add(new File("C:\\Users\\Jakob\\Desktop\\Pupillometrie_Studie_Thomas_18012017"));
	}

	public static void main(String[] args) {
		try {
			PupillometryFileMerger merger = new PupillometryFileMerger(PupillometryFile.SEPARATOR_TABULATOR);

			for (File folder : FOLDERS) {
				System.out.println("Scanning directory: " + folder.getName());
				if (!folder.isDirectory()) {
					System.out.println(folder.getName() + " is not a directory - skipped.");
					continue;
				}

				File[] files = folder.listFiles();
				for (File file : files) {
					if (!file.getName().endsWith("tsv")) {
						System.out.println("Skipping file: " + file.getName());
						continue;
					}

					System.out.println("Adding file: " + file.getName());

					PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true, ",");
					merger.addFile(pupillometryFile);
				}
			}

			System.out.println("Writing output to " + TARGET);
			PupillometryFile mergedFile = merger.getPupillometryFile();
			File targetFile = new File(TARGET);
			mergedFile.writeToFile(targetFile);
			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
