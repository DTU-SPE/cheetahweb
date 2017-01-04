package org.cheetahplatform.web.util;

import java.io.File;

public class FileRenamer {

	private static final String FOLDER = "C:\\Studie_Thomas\\Session002_Erotica";
	private static final String APPENDIX = "_Erotica@Studie_Thomas";

	public static void main(String[] args) {
		File folder = new File(FOLDER);
		File[] children = folder.listFiles();
		for (File file : children) {
			String name = file.getName();
			String fileExtension = FileUtils.getFileExtension(name);
			String basicName = FileUtils.getFileNameWithoutExtension(name);

			String newName = basicName + APPENDIX + fileExtension;
			file.renameTo(new File(folder, newName));
		}
	}
}
