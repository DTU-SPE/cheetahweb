package org.cheetahplatform.web.util;

import java.io.File;

public class ExtractUserNamesFromFiles {
	private static final String ROOT = "D:\\tmp\\processed_MRT\\other";

	public static void main(String[] args) {
		File root = new File(ROOT);
		if (!root.isDirectory()) {
			System.err.println("This is not a directory! Exit.");
			return;
		}

		File[] files = root.listFiles();
		for (File file : files) {
			String[] split = file.getName().split("@");
			System.out.println(split[0]);
		}
	}
}
